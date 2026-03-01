import type { API, FileInfo, Options } from 'jscodeshift';

// Use dynamic import check — content-tag is optional
let Preprocessor: any;
try {
  Preprocessor = require('content-tag').Preprocessor;
} catch {
  // content-tag not available — .gts support disabled
}

/**
 * Regex patterns for placeholder replacement.
 * These match the content-tag-utils placeholder format.
 */
const EXPRESSION_PLACEHOLDER_RE = /TEMPLATE_TEMPLATE\(`([\s\S]*?)`\)/g;
const CLASS_MEMBER_PLACEHOLDER_RE = /\[_TEMPLATE_\(`([\s\S]*?)`\)\]\s*=\s*0;?/g;

/**
 * Check if a file is a .gts or .gjs file.
 */
export function isGtsFile(filePath: string): boolean {
  return filePath.endsWith('.gts') || filePath.endsWith('.gjs');
}

/**
 * Replace <template> blocks with valid TS/JS placeholders.
 * Returns the modified source and template metadata for later reassembly.
 *
 * Expression-position templates become:     TEMPLATE_TEMPLATE(`...`)
 * Class-member-position templates become:   [_TEMPLATE_(`...`)] = 0;
 */
export function extractTemplates(source: string): {
  processedSource: string;
  hasTemplates: boolean;
} {
  if (!Preprocessor) {
    return { processedSource: source, hasTemplates: false };
  }

  const preprocessor = new Preprocessor();
  let parsed: any[];
  try {
    parsed = preprocessor.parse(source);
  } catch {
    return { processedSource: source, hasTemplates: false };
  }

  if (parsed.length === 0) {
    return { processedSource: source, hasTemplates: false };
  }

  // Process templates in reverse order to maintain correct byte offsets
  let result = source;
  for (let i = parsed.length - 1; i >= 0; i--) {
    const p = parsed[i];
    const startByte = p.range.startByte ?? p.range.start;
    const endByte = p.range.endByte ?? p.range.end;

    // Extract the content between <template> tags
    const contentStart = p.contentRange.start ?? p.contentRange.startByte;
    const contentEnd = p.contentRange.end ?? p.contentRange.endByte;
    const content = source.slice(contentStart, contentEnd);

    // Escape backticks and dollar signs in template content for the placeholder
    // template literal. Dollar sign escaping is round-trip safe because:
    // - Glimmer templates use {{...}} for expressions, not ${...}, so `$` is rare.
    // - A literal `$` is escaped to `\$` on extract and restored on output.
    // - If jscodeshift normalizes `\$` back to `$` (since `\$` is a no-op escape
    //   in JS template literals), the restore regex simply doesn't match and the
    //   bare `$` passes through unchanged — which is the correct original content.
    // - Literal `\$` in template content (backslash + dollar) is preserved because
    //   only `$` is matched by the regex, producing `\\$` which restores correctly.
    const escapedContent = content.replace(/`/g, '\\`').replace(/\$/g, '\\$');

    let placeholder: string;
    if (p.type === 'expression') {
      placeholder = `TEMPLATE_TEMPLATE(\`${escapedContent}\`)`;
    } else {
      // class-member
      placeholder = `[_TEMPLATE_(\`${escapedContent}\`)] = 0;`;
    }

    result = result.slice(0, startByte) + placeholder + result.slice(endByte);
  }

  return { processedSource: result, hasTemplates: true };
}

/**
 * Restore <template> blocks from placeholders after jscodeshift transform.
 */
export function restoreTemplates(transformedSource: string, originalSource?: string): string {
  let result = transformedSource;

  // Restore expression-position templates
  result = result.replace(
    EXPRESSION_PLACEHOLDER_RE,
    (_match, content) => {
      const unescaped = content.replace(/\\`/g, '`').replace(/\\\$/g, '$');
      return `<template>${unescaped}</template>`;
    },
  );

  // Restore class-member-position templates
  result = result.replace(
    CLASS_MEMBER_PLACEHOLDER_RE,
    (_match, content) => {
      const unescaped = content.replace(/\\`/g, '`').replace(/\\\$/g, '$');
      return `<template>${unescaped}</template>`;
    },
  );

  // Validate that all placeholders were restored
  const unresolvedPlaceholders = findUnresolvedPlaceholders(result);
  if (unresolvedPlaceholders.length > 0) {
    console.warn(
      `[gts-support] WARNING: ${unresolvedPlaceholders.length} template placeholder(s) were not restored, reverting to original. ` +
      `This may indicate that jscodeshift reformatted the placeholder code. ` +
      `Unresolved placeholders: ${unresolvedPlaceholders.join(', ')}`,
    );
    // Return the original source unchanged to avoid corrupted output
    if (originalSource !== undefined) {
      return originalSource;
    }
  }

  return result;
}

/**
 * Check for any remaining placeholder strings that were not restored.
 * Returns an array of unresolved placeholder fragments found in the source.
 */
export function findUnresolvedPlaceholders(source: string): string[] {
  const unresolved: string[] = [];

  // Check for expression-position placeholder fragments
  if (source.includes('TEMPLATE_TEMPLATE(')) {
    unresolved.push('TEMPLATE_TEMPLATE(...)');
  }

  // Check for class-member-position placeholder fragments
  if (source.includes('[_TEMPLATE_(')) {
    unresolved.push('[_TEMPLATE_(...)]');
  }

  return unresolved;
}

/**
 * Wraps a standard jscodeshift transform to add .gts/.gjs file support.
 *
 * Usage: instead of exporting your transform directly, wrap it:
 *   export default withGtsSupport(myTransform);
 *
 * How it works:
 * 1. Detects .gts/.gjs files by extension
 * 2. Replaces <template> blocks with valid JS/TS placeholders
 * 3. Runs the original jscodeshift transform on the valid code
 * 4. Restores <template> blocks in the output
 */
export function withGtsSupport(
  transform: (fileInfo: FileInfo, api: API, options: Options) => string | undefined,
) {
  return function gtsAwareTransform(
    fileInfo: FileInfo,
    api: API,
    options: Options,
  ): string | undefined {
    if (!isGtsFile(fileInfo.path)) {
      // Regular .ts/.js file — pass through directly
      return transform(fileInfo, api, options);
    }

    // Extract templates, replacing them with valid JS/TS placeholders
    const { processedSource, hasTemplates } = extractTemplates(fileInfo.source);

    if (!hasTemplates) {
      // No templates found — treat as regular TS
      return transform(fileInfo, api, options);
    }

    // Run the original transform on the placeholder version
    const modifiedFileInfo: FileInfo = {
      ...fileInfo,
      source: processedSource,
    };

    const result = transform(modifiedFileInfo, api, options);

    if (result === undefined) {
      // Transform made no changes
      return undefined;
    }

    // Restore the <template> blocks, passing the original source as fallback
    return restoreTemplates(result, fileInfo.source);
  };
}
