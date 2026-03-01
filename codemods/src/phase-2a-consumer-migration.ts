import type { API, FileInfo, Options } from 'jscodeshift';
import { isToArray, isSortBy, buildSortComparator, buildMultiKeySortComparator, isValidIdentifier } from './utils/ember-apis';
import { isUsedOnlyAsType } from './utils/imports';
import { withGtsSupport } from './utils/gts-support';

/**
 * Phase 2a: Consumer Migration
 *
 * Update consumer files (routes, controllers, components) that reference ember-data APIs.
 * Transformations:
 *  1. import Store from '@ember-data/store' → import type Store from 'APP_NAME/services/store'
 *  2. Value imports used only as types → import type
 *  3. data.toArray() → Array.from(data)
 *  4. arr.sortBy('key') → Array.from(arr).sort((a, b) => a.key - b.key)
 */

interface Phase2aOptions {
  appName?: string;
}

function transformer(
  fileInfo: FileInfo,
  api: API,
  options: Options,
): string | undefined {
  const j = api.jscodeshift;
  const root = j(fileInfo.source);
  let changed = false;

  const phase2aOpts: Phase2aOptions = options as any;
  const appName = phase2aOpts.appName ?? 'app';

  // --- 1: Store import migration ---

  root
    .find(j.ImportDeclaration, { source: { value: '@ember-data/store' } })
    .forEach((path) => {
      path.node.source = j.literal(`${appName}/services/store`);
      // Only make it type-only if all specifiers are used only in type positions
      const specs = path.node.specifiers ?? [];
      const allTypeOnly = specs.length > 0 && specs.every((spec) => {
        const localName = spec.local?.name;
        if (!localName) return false;
        return isUsedOnlyAsType(j, root, localName);
      });
      if (allTypeOnly) {
        path.node.importKind = 'type';
      }
      changed = true;
    });

  // Also handle already-migrated store import that's still a value import
  root
    .find(j.ImportDeclaration, {
      source: { value: `${appName}/services/store` },
    })
    .forEach((path) => {
      if (path.node.importKind !== 'type') {
        // Only make it type-only if all specifiers are used only in type positions
        const specs = path.node.specifiers ?? [];
        const allTypeOnly = specs.length > 0 && specs.every((spec) => {
          const localName = spec.local?.name;
          if (!localName) return false;
          return isUsedOnlyAsType(j, root, localName);
        });
        if (allTypeOnly) {
          path.node.importKind = 'type';
          changed = true;
        }
      }
    });

  // --- 2: Convert model value imports to type imports ---

  root.find(j.ImportDeclaration).forEach((path) => {
    const source = (path.node.source as any).value ?? '';

    // Only process model imports
    if (!source.includes('/models/')) return;

    // Skip if already a type import
    if (path.node.importKind === 'type') return;

    // Check each specifier
    const specs = path.node.specifiers ?? [];
    if (specs.length === 0) return;

    // Check if ALL specifiers are used only as types
    const allTypeOnly = specs.every((spec) => {
      const localName = spec.local?.name;
      if (!localName) return false;
      return isUsedOnlyAsType(j, root, localName);
    });

    if (allTypeOnly) {
      path.node.importKind = 'type';
      changed = true;
    }
  });

  // --- 3: .toArray() → Array.from(...) ---

  root.find(j.CallExpression).forEach((path) => {
    const node = path.node;

    const toArr = isToArray(j, node);
    if (toArr) {
      j(path).replaceWith(
        j.callExpression(
          j.memberExpression(j.identifier('Array'), j.identifier('from')),
          [toArr.receiver],
        ),
      );
      changed = true;
      return;
    }

    // --- 4: .sortBy('key') → Array.from(...).sort((a, b) => a.key - b.key) ---

    const sortByMatch = isSortBy(j, node);
    if (sortByMatch) {
      let comparator;
      if (sortByMatch.keys && sortByMatch.keys.length > 1) {
        if (sortByMatch.keys.some(k => !isValidIdentifier(k))) return;
        comparator = buildMultiKeySortComparator(j, sortByMatch.keys);
      } else {
        if (!isValidIdentifier(sortByMatch.key)) return;
        comparator = buildSortComparator(j, sortByMatch.key);
      }
      const arrayFrom = j.callExpression(
        j.memberExpression(j.identifier('Array'), j.identifier('from')),
        [sortByMatch.receiver],
      );
      const sortCall = j.callExpression(
        j.memberExpression(arrayFrom, j.identifier('sort')),
        [comparator],
      );
      j(path).replaceWith(sortCall);
      changed = true;
      return;
    }
  });

  if (!changed) return undefined;
  return root.toSource({ quote: 'single' });
}

export default withGtsSupport(transformer);
