#!/usr/bin/env node

/**
 * Post-migration diagnostic scanner.
 *
 * Scans a target directory for common issues after running the codemod suite.
 *
 * Usage:
 *   npx tsx codemods/src/post-check.ts --target=frontend/app
 *   npx tsx codemods/src/post-check.ts --target=frontend/app --strict
 *   npx tsx codemods/src/post-check.ts --target=frontend/app --json
 */

import * as fs from 'fs';
import * as path from 'path';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export type CheckStatus = 'pass' | 'warn' | 'fail';

export interface CheckResult {
  name: string;
  status: CheckStatus;
  message?: string;
  locations?: string[];
}

export interface PostCheckOptions {
  target: string;
  strict: boolean;
  json: boolean;
  verbose: boolean;
}

// ---------------------------------------------------------------------------
// Filesystem helpers
// ---------------------------------------------------------------------------

export function walkDir(dir: string): string[] {
  const results: string[] = [];
  if (!fs.existsSync(dir)) return results;

  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      results.push(...walkDir(fullPath));
    } else {
      results.push(fullPath);
    }
  }
  return results;
}

function tsFiles(dir: string): string[] {
  return walkDir(dir).filter(
    (f) => f.endsWith('.ts') || f.endsWith('.gts'),
  );
}

// Simple per-run cache to avoid reading the same file multiple times across checks
const fileContentCache = new Map<string, string>();

function readFile(filePath: string): string {
  let content = fileContentCache.get(filePath);
  if (content === undefined) {
    content = fs.readFileSync(filePath, 'utf-8');
    fileContentCache.set(filePath, content);
  }
  return content;
}

// ---------------------------------------------------------------------------
// Helper: scan files for a line-level pattern
// ---------------------------------------------------------------------------

function scanFilesForPattern(
  target: string,
  dir: string,
  pattern: RegExp,
): string[] {
  const files = tsFiles(dir);
  const matches: string[] = [];
  for (const filePath of files) {
    const content = readFile(filePath);
    const lines = content.split('\n');
    for (let i = 0; i < lines.length; i++) {
      if (pattern.test(lines[i])) {
        matches.push(`${path.relative(target, filePath)}:${i + 1}`);
      }
    }
  }
  return matches;
}

// ---------------------------------------------------------------------------
// Helper: scan files for a file-level pattern
// ---------------------------------------------------------------------------

function scanFilesForContent(
  target: string,
  dir: string,
  pattern: RegExp,
): string[] {
  const files = tsFiles(dir);
  const matches: string[] = [];
  for (const filePath of files) {
    const content = readFile(filePath);
    if (pattern.test(content)) {
      matches.push(path.relative(target, filePath));
    }
  }
  return matches;
}

// ---------------------------------------------------------------------------
// Individual checks
// ---------------------------------------------------------------------------

export function checkStoreService(target: string): CheckResult {
  const candidates = ['services/store.ts', 'services/store.js'];
  for (const rel of candidates) {
    if (fs.existsSync(path.join(target, rel))) {
      return { name: 'Store service exists', status: 'pass' };
    }
  }
  return {
    name: 'Store service exists',
    status: 'fail',
    message: 'Missing: create app/services/store.ts with useLegacyStore() and RequestManager.',
  };
}

export function checkWarpDriveInstall(target: string): CheckResult {
  const candidates = ['app.ts', 'app.js', 'app.gts'];
  for (const rel of candidates) {
    const filePath = path.join(target, rel);
    if (fs.existsSync(filePath)) {
      const content = readFile(filePath);
      if (content.includes('@warp-drive/ember/install')) {
        return { name: '@warp-drive/ember/install', status: 'pass' };
      }
    }
  }
  return {
    name: '@warp-drive/ember/install',
    status: 'fail',
    message: "Missing: add `import '@warp-drive/ember/install'` to app entry file.",
  };
}

export function checkRemainingEmberDataImports(target: string): CheckResult {
  const matches = scanFilesForContent(target, target, /@ember-data\//);
  if (matches.length === 0) {
    return { name: 'Remaining @ember-data/ imports', status: 'pass' };
  }
  return {
    name: `Remaining @ember-data/ imports (${matches.length} files)`,
    status: 'warn',
    locations: matches,
  };
}

export function checkRemainingBarrelImports(target: string): CheckResult {
  const matches = scanFilesForContent(target, target, /from\s+['"]ember-data['"]/);
  if (matches.length === 0) {
    return { name: 'Remaining ember-data barrel imports', status: 'pass' };
  }
  return {
    name: `Remaining ember-data barrel imports (${matches.length} files)`,
    status: 'warn',
    locations: matches,
  };
}

export function checkRemainingEmberUtils(target: string): CheckResult {
  const pattern = /from\s+['"]@ember\/utils['"]/;
  const matches = scanFilesForContent(target, target, pattern);
  if (matches.length === 0) {
    return { name: 'Remaining @ember/utils imports', status: 'pass' };
  }
  return {
    name: `Remaining @ember/utils imports (${matches.length} files)`,
    status: 'warn',
    message: '@ember/utils should have been removed by phase 0. Replace with native JS equivalents.',
    locations: matches,
  };
}

export function checkRemainingEmberArray(target: string): CheckResult {
  const pattern = /from\s+['"]@ember\/array['"]/;
  const matches = scanFilesForContent(target, target, pattern);
  if (matches.length === 0) {
    return { name: 'Remaining @ember/array imports', status: 'pass' };
  }
  return {
    name: `Remaining @ember/array imports (${matches.length} files)`,
    status: 'warn',
    message: '@ember/array should have been removed by phase 0. Replace with native JS arrays.',
    locations: matches,
  };
}

export function checkCodemodTodos(target: string): CheckResult {
  const todoPattern =
    /TODO.*(?:codemod|@warp-drive|@ember-data|ember-data|self\.|wire.*service|register.*schema|verify.*import|model.*type.*argument)|CODEMOD.*TODO/i;
  const matches = scanFilesForPattern(target, target, todoPattern);
  if (matches.length === 0) {
    return { name: 'Codemod-related TODO comments', status: 'pass' };
  }
  return {
    name: `Codemod-related TODO comments (${matches.length} found)`,
    status: 'warn',
    locations: matches,
  };
}

export function checkInverseNull(target: string): CheckResult {
  const schemasDir = path.join(target, 'schemas');
  if (!fs.existsSync(schemasDir)) {
    return { name: 'inverse: null relationships', status: 'pass' };
  }
  const matches = scanFilesForPattern(target, schemasDir, /inverse:\s*null/);
  if (matches.length === 0) {
    return { name: 'inverse: null relationships', status: 'pass' };
  }
  return {
    name: `inverse: null relationships (${matches.length} found)`,
    status: 'warn',
    locations: matches,
  };
}

export function checkExtensionThisToSelf(target: string): CheckResult {
  const schemasDir = path.join(target, 'schemas');
  if (!fs.existsSync(schemasDir)) {
    return { name: 'Extension this. -> self. TODOs', status: 'pass' };
  }
  const matches = scanFilesForPattern(
    target,
    schemasDir,
    /TODO.*this\.\s*.*self\.|TODO.*rewrite.*this/i,
  );
  if (matches.length === 0) {
    return { name: 'Extension this. -> self. TODOs', status: 'pass' };
  }
  return {
    name: `Extension this. -> self. TODOs (${matches.length} found)`,
    status: 'warn',
    locations: matches,
  };
}

// ---------------------------------------------------------------------------
// Deprecated API checks (from PR #2809 review)
// ---------------------------------------------------------------------------

export function checkRemainingDeprecatedApis(target: string): CheckResult {
  // Detect .toArray(), .sortBy(), .filterBy(), .mapBy(), .findBy(),
  // .pushObject(), .pushObjects(), .removeObject(), .removeObjects(),
  // .uniq(), .firstObject, .lastObject
  const pattern =
    /\.(toArray|sortBy|filterBy|mapBy|findBy|pushObject|pushObjects|removeObject|removeObjects|uniq)\s*\(|\.firstObject\b|\.lastObject\b/;
  const matches = scanFilesForPattern(target, target, pattern);
  if (matches.length === 0) {
    return { name: 'Remaining deprecated array APIs', status: 'pass' };
  }
  return {
    name: `Remaining deprecated array APIs (${matches.length} found)`,
    status: 'warn',
    message: '.toArray(), .sortBy(), .filterBy(), .mapBy(), .findBy(), .pushObject(), .uniq(), .firstObject, .lastObject',
    locations: matches,
  };
}

export function checkRemainingTransitionMethods(target: string): CheckResult {
  // Detect this.transitionTo() / this.replaceWith() — deprecated Route/Controller methods
  const pattern = /this\.(transitionTo|replaceWith)\s*\(/;
  const matches = scanFilesForPattern(target, target, pattern);
  if (matches.length === 0) {
    return { name: 'Remaining this.transitionTo/replaceWith', status: 'pass' };
  }
  return {
    name: `Remaining this.transitionTo/replaceWith (${matches.length} found)`,
    status: 'warn',
    message: 'Use this.router.transitionTo() / this.router.replaceWith() instead.',
    locations: matches,
  };
}

export function checkRemainingGetSet(target: string): CheckResult {
  // Detect this.get('...') and this.set('...', ...) — Ember computed property access
  // Also detect obj.get('...') on non-this receivers
  const callPattern = /\.\s*(get|set)\s*\(\s*['"]/;
  // Detect import { get } from '@ember/object' or import { set } from '@ember/object'
  const importPattern =
    /import\s+\{[^}]*\b(?:get|set)\b[^}]*\}\s+from\s+['"]@ember\/object['"]/;

  const callMatches = scanFilesForPattern(target, target, callPattern);
  const importMatches = scanFilesForPattern(target, target, importPattern);

  // Deduplicate in case both patterns match the same line
  const allMatchesSet = new Set([...callMatches, ...importMatches]);
  const matches = Array.from(allMatchesSet).sort();

  if (matches.length === 0) {
    return { name: 'Remaining get()/set() usage', status: 'pass' };
  }
  return {
    name: `Remaining get()/set() usage (${matches.length} found)`,
    status: 'warn',
    message:
      'Use direct property access instead: this.prop / this.prop = value. Remove @ember/object get/set imports.',
    locations: matches,
  };
}

export function checkRemainingSetProperties(target: string): CheckResult {
  // Detect .setProperties() — incompatible with SchemaRecord
  const pattern = /\.setProperties\s*\(/;
  const matches = scanFilesForPattern(target, target, pattern);
  if (matches.length === 0) {
    return { name: 'Remaining .setProperties()', status: 'pass' };
  }
  return {
    name: `Remaining .setProperties() (${matches.length} found)`,
    status: 'warn',
    message: 'SchemaRecord does not support setProperties(). Assign properties individually or pass data to createRecord().',
    locations: matches,
  };
}

export function checkRemainingAdapters(target: string): CheckResult {
  const adaptersDir = path.join(target, 'adapters');
  if (!fs.existsSync(adaptersDir)) {
    return { name: 'Remaining adapter files', status: 'pass' };
  }
  const files = tsFiles(adaptersDir);
  if (files.length === 0) {
    return { name: 'Remaining adapter files', status: 'pass' };
  }
  const locations = files.map((f) => path.relative(target, f));
  return {
    name: `Remaining adapter files (${files.length} found)`,
    status: 'warn',
    message: 'Adapters should be replaced with RequestManager handlers.',
    locations,
  };
}

export function checkRemainingSerializers(target: string): CheckResult {
  const serializersDir = path.join(target, 'serializers');
  if (!fs.existsSync(serializersDir)) {
    return { name: 'Remaining serializer files', status: 'pass' };
  }
  const files = tsFiles(serializersDir);
  if (files.length === 0) {
    return { name: 'Remaining serializer files', status: 'pass' };
  }
  const locations = files.map((f) => path.relative(target, f));
  return {
    name: `Remaining serializer files (${files.length} found)`,
    status: 'warn',
    message: 'Serializers should be replaced with RequestManager handlers.',
    locations,
  };
}

export function checkRemainingOldTransforms(target: string): CheckResult {
  // Detect old-style DS.Transform / Transform subclasses in app/transforms/
  const transformsDir = path.join(target, 'transforms');
  if (!fs.existsSync(transformsDir)) {
    return { name: 'Remaining legacy transforms', status: 'pass' };
  }
  const files = tsFiles(transformsDir);
  if (files.length === 0) {
    return { name: 'Remaining legacy transforms', status: 'pass' };
  }
  const locations = files.map((f) => path.relative(target, f));
  return {
    name: `Remaining legacy transforms (${files.length} found)`,
    status: 'warn',
    message: 'Migrate to WarpDrive Transformations (app/transformations/) with hydrate/serialize.',
    locations,
  };
}

export function checkModelImportsNotRewrittenToSchemas(target: string): CheckResult {
  // Detect consumer files still importing from app/models/ when they could
  // import directly from app/schemas/. After phase 3a, model files become
  // thin re-export stubs — consumers should import from schemas/ directly.
  //
  // Matches patterns like:
  //   import User from 'myapp/models/user'
  //   import type User from 'myapp/models/user'
  //   import { IUser } from 'myapp/models/user'
  //
  // Excludes: the model stub files themselves (inside models/)
  const modelsDir = path.join(target, 'models');
  const schemasDir = path.join(target, 'schemas');
  if (!fs.existsSync(schemasDir)) {
    // No schemas directory means phase 3a hasn't run — nothing to check
    return { name: 'Model imports rewritten to schemas', status: 'pass' };
  }

  // Collect files outside models/ and schemas/ directories
  const allFiles = tsFiles(target);
  const consumerFiles = allFiles.filter(
    (f) =>
      !f.startsWith(modelsDir + path.sep) &&
      !f.startsWith(schemasDir + path.sep) &&
      // Also skip the models dir itself if it matches exactly
      f !== modelsDir,
  );

  // Match import from '...models/...' or from "...models/..."
  // This is intentionally broad — catches both relative and app-name imports
  const importFromModelsPattern = /from\s+['"][^'"]*\/models\//;
  const matches: string[] = [];

  for (const filePath of consumerFiles) {
    const content = readFile(filePath);
    const lines = content.split('\n');
    for (let i = 0; i < lines.length; i++) {
      if (importFromModelsPattern.test(lines[i])) {
        matches.push(`${path.relative(target, filePath)}:${i + 1}`);
      }
    }
  }

  if (matches.length === 0) {
    return { name: 'Model imports rewritten to schemas', status: 'pass' };
  }
  return {
    name: `Model imports not yet rewritten to schemas (${matches.length} found)`,
    status: 'warn',
    message: "Import from 'app/schemas/X' instead of 'app/models/X'. Model files are now stubs.",
    locations: matches,
  };
}

// ---------------------------------------------------------------------------
// Summary printing
// ---------------------------------------------------------------------------

const STATUS_LABELS: Record<CheckStatus, string> = {
  pass: '[PASS]',
  warn: '[WARN]',
  fail: '[FAIL]',
};

export function printCheckResults(
  results: CheckResult[],
  opts?: { verbose?: boolean },
): void {
  const verbose = opts?.verbose ?? true;

  console.log('');
  console.log('=== Post-Migration Checklist ===');
  console.log('');

  for (const r of results) {
    console.log(`${STATUS_LABELS[r.status]} ${r.name}`);
    if (r.message) {
      console.log(`       ${r.message}`);
    }
    if (verbose && r.locations && r.locations.length > 0) {
      for (const loc of r.locations) {
        console.log(`       ${loc}`);
      }
    } else if (!verbose && r.locations && r.locations.length > 0) {
      console.log(`       (use --verbose to see all locations)`);
    }
  }

  const passed = results.filter((r) => r.status === 'pass').length;
  const warnings = results.filter((r) => r.status === 'warn').length;
  const failures = results.filter((r) => r.status === 'fail').length;

  const manualItems = results.reduce(
    (n, r) => n + (r.locations?.length ?? (r.status !== 'pass' ? 1 : 0)),
    0,
  );

  console.log('');
  console.log(
    `Summary: ${passed} passed, ${warnings} warnings, ${failures} failures`,
  );
  if (manualItems > 0) {
    console.log(`  ${manualItems} items need manual attention`);
  }
}

export function formatCheckResultsJson(results: CheckResult[]): string {
  const passed = results.filter((r) => r.status === 'pass').length;
  const warnings = results.filter((r) => r.status === 'warn').length;
  const failures = results.filter((r) => r.status === 'fail').length;
  const manualItems = results.reduce(
    (n, r) => n + (r.locations?.length ?? (r.status !== 'pass' ? 1 : 0)),
    0,
  );

  return JSON.stringify(
    {
      results: results.map((r) => ({
        name: r.name,
        status: r.status,
        message: r.message ?? null,
        locations: r.locations ?? [],
      })),
      summary: { passed, warnings, failures, manualItems },
    },
    null,
    2,
  );
}

// ---------------------------------------------------------------------------
// Arg parsing
// ---------------------------------------------------------------------------

export function parsePostCheckArgs(argv: string[]): PostCheckOptions {
  let target = '';
  let strict = false;
  let json = false;
  let verbose = false;

  for (const arg of argv) {
    if (arg.startsWith('--target=')) {
      target = arg.slice('--target='.length);
    } else if (arg === '--strict') {
      strict = true;
    } else if (arg === '--json') {
      json = true;
    } else if (arg === '--verbose') {
      verbose = true;
    }
  }

  return { target, strict, json, verbose };
}

// ---------------------------------------------------------------------------
// Run all checks
// ---------------------------------------------------------------------------

export function runAllChecks(resolvedTarget: string): CheckResult[] {
  fileContentCache.clear();
  return [
    checkStoreService(resolvedTarget),
    checkWarpDriveInstall(resolvedTarget),
    checkRemainingEmberDataImports(resolvedTarget),
    checkRemainingBarrelImports(resolvedTarget),
    checkRemainingEmberUtils(resolvedTarget),
    checkRemainingEmberArray(resolvedTarget),
    checkCodemodTodos(resolvedTarget),
    checkInverseNull(resolvedTarget),
    checkExtensionThisToSelf(resolvedTarget),
    checkRemainingDeprecatedApis(resolvedTarget),
    checkRemainingTransitionMethods(resolvedTarget),
    checkRemainingGetSet(resolvedTarget),
    checkRemainingSetProperties(resolvedTarget),
    checkRemainingAdapters(resolvedTarget),
    checkRemainingSerializers(resolvedTarget),
    checkRemainingOldTransforms(resolvedTarget),
    checkModelImportsNotRewrittenToSchemas(resolvedTarget),
  ];
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

function main(): void {
  const opts = parsePostCheckArgs(process.argv.slice(2));

  if (!opts.target) {
    console.error('Usage: post-check.ts --target=path/to/app [--strict] [--json] [--verbose]');
    process.exit(1);
  }

  const resolvedTarget = path.resolve(opts.target);
  if (!fs.existsSync(resolvedTarget)) {
    console.error(`Target directory not found: ${resolvedTarget}`);
    process.exit(1);
  }

  const results = runAllChecks(resolvedTarget);

  if (opts.json) {
    console.log(formatCheckResultsJson(results));
  } else {
    printCheckResults(results, { verbose: opts.verbose });
  }

  const failures = results.filter((r) => r.status === 'fail').length;
  const warnings = results.filter((r) => r.status === 'warn').length;

  if (failures > 0) {
    process.exit(1);
  }
  if (opts.strict && warnings > 0) {
    if (!opts.json) {
      console.log('Exiting with error due to --strict mode (warnings treated as failures).');
    }
    process.exit(1);
  }
}

if (require.main === module) {
  main();
}
