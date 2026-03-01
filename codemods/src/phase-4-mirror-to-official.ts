import type { API, FileInfo, Options } from 'jscodeshift';
import { withGtsSupport } from './utils/gts-support';

/**
 * Phase 4: Mirror to Official Package Migration
 *
 * Simple import source replacement for transitioning from mirror packages
 * to official @warp-drive/* packages.
 *
 * Transformation:
 *   @warp-drive-mirror/* → @warp-drive/*
 */
function transformer(
  fileInfo: FileInfo,
  api: API,
  _options: Options,
): string | undefined {
  const j = api.jscodeshift;
  const root = j(fileInfo.source);
  let changed = false;

  // 1. Static import declarations
  root.find(j.ImportDeclaration).forEach((path) => {
    const source = path.node.source;
    const value = (source as any).value;

    if (typeof value === 'string' && value.includes('@warp-drive-mirror/')) {
      const newValue = value.replace(/@warp-drive-mirror\//g, '@warp-drive/');
      path.node.source = j.literal(newValue);
      changed = true;
    }
  });

  // 2. require() calls: require('@warp-drive-mirror/...')
  root
    .find(j.CallExpression, {
      callee: { type: 'Identifier', name: 'require' },
    })
    .forEach((path) => {
      const args = path.node.arguments;
      if (args.length > 0 && j.StringLiteral.check(args[0])) {
        const value = (args[0] as any).value as string;
        if (value.includes('@warp-drive-mirror/')) {
          const newValue = value.replace(/@warp-drive-mirror\//g, '@warp-drive/');
          args[0] = j.literal(newValue);
          changed = true;
        }
      }
    });

  // 3. Dynamic import() calls: import('@warp-drive-mirror/...')
  // Modern parsers emit ImportExpression nodes for dynamic import().
  if (j.ImportExpression) {
    root.find(j.ImportExpression).forEach((path) => {
      const source = (path.node as any).source;
      if (j.StringLiteral.check(source)) {
        const value = source.value as string;
        if (value.includes('@warp-drive-mirror/')) {
          const newValue = value.replace(/@warp-drive-mirror\//g, '@warp-drive/');
          (path.node as any).source = j.literal(newValue);
          changed = true;
        }
      }
    });
  }

  // Fallback for parsers that represent dynamic import() as CallExpression
  // with an Import callee.
  root
    .find(j.CallExpression, {
      callee: { type: 'Import' },
    })
    .forEach((path) => {
      const args = path.node.arguments;
      if (args.length > 0 && j.StringLiteral.check(args[0])) {
        const value = (args[0] as any).value as string;
        if (value.includes('@warp-drive-mirror/')) {
          const newValue = value.replace(/@warp-drive-mirror\//g, '@warp-drive/');
          args[0] = j.literal(newValue);
          changed = true;
        }
      }
    });

  if (!changed) return undefined;
  return root.toSource({ quote: 'single' });
}

export default withGtsSupport(transformer);
