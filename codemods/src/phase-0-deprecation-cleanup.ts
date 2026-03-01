import type { API, FileInfo, Options } from 'jscodeshift';
import {
  isThisGet,
  isThisSet,
  isThisSetTemplateLiteral,
  isObjectGet,
  isObjectSet,
  isEmberGetFunctional,
  isEmberSetFunctional,
  isToArray,
  isSortBy,
  isMapBy,
  isFilterBy,
  isUniq,
  isFindBy,
  isPushObject,
  isRemoveObject,
  isSetProperties,
  isEmberIsEmpty,
  isEmberIsPresent,
  isEmberIsNone,
  isThisTransitionTo,
  isThisReplaceWith,
  isHasManyValue,
  isHasManyIds,
  isBelongsToValue,
  buildThisProp,
  buildOptionalChain,
  buildOptionalChainOnReceiver,
  buildSortComparator,
  buildMultiKeySortComparator,
  isSimpleExpression,
  isValidIdentifier,
} from './utils/ember-apis';
import { removeImport, addImport } from './utils/imports';
import { withGtsSupport } from './utils/gts-support';

/**
 * Deep-clone an AST node to avoid reusing the same reference in multiple
 * positions of a replacement tree (which can confuse jscodeshift serialization).
 */
function cloneNode<T>(node: T): T {
  return JSON.parse(JSON.stringify(node));
}

/**
 * Phase 0: Deprecation Cleanup
 *
 * Mechanical cleanup of deprecated Ember/ember-data APIs before migration.
 * Transformations:
 *  1.  this.get('prop')              → this.prop
 *  2.  this.get('nested.path')       → this.nested?.path
 *  3.  this.set('prop', val)         → this.prop = val
 *  3b. this.set(`${x}Prop`, val)     → this[`${x}Prop`] = val
 *  3c. obj.get('prop')               → obj.prop / obj.a?.b
 *  3d. get(obj, 'prop')              → obj.prop (functional Ember.get)
 *  3e. set(obj, 'prop', val)         → obj.prop = val (functional Ember.set)
 *  4.  .toArray()                    → Array.from(...)
 *  5.  .sortBy('key')               → .sort((a, b) => a.key < b.key ? -1 : ...)
 *  6.  .mapBy('key')                → .map(item => item.key)
 *  7.  .filterBy('key', val)         → .filter(item => item.key === val)
 *  8.  .uniq()                       → [...new Set(...)]
 *  9.  isEmpty(x)                    → x == null || x === '' || (Array.isArray(x) && x.length === 0)
 *  9b. isPresent(x)                  → x != null && x !== '' && (!Array.isArray(x) || x.length > 0)
 *  9c. isNone(x)                     → x == null
 * 10.  this.transitionTo(...)        → this.router.transitionTo(...)
 * 11.  this.replaceWith(...)         → this.router.replaceWith(...)
 * 12.  model.hasMany('rel').value()  → model.rel
 * 13.  model.hasMany('rel').ids()    → (model.rel || []).map(r => r.id)
 * 14.  model.belongsTo('rel').value() → model.rel
 */
function transformer(
  fileInfo: FileInfo,
  api: API,
  _options: Options,
): string | undefined {
  const j = api.jscodeshift;
  const root = j(fileInfo.source);
  let changed = false;

  // Check if functional get/set are imported from @ember/object.
  // Resolve local names to handle aliased imports like `import { get as emberGet } from '@ember/object'`.
  const getLocalName = resolveImportLocalName(j, root, 'get', '@ember/object');
  const setLocalName = resolveImportLocalName(j, root, 'set', '@ember/object');
  const hasEmberGetImport = getLocalName !== null;
  const hasEmberSetImport = setLocalName !== null;

  // Check if A/isArray are imported from @ember/array.
  // Resolve local names to handle aliased imports.
  const aLocalName = resolveImportLocalName(j, root, 'A', '@ember/array');
  const isArrayLocalName = resolveImportLocalName(j, root, 'isArray', '@ember/array');
  const hasEmberA = aLocalName !== null;
  const hasEmberIsArray = isArrayLocalName !== null;

  // Resolve local names for @ember/utils helpers.
  const isEmptyLocalName = resolveImportLocalName(j, root, 'isEmpty', '@ember/utils');
  const isPresentLocalName = resolveImportLocalName(j, root, 'isPresent', '@ember/utils');
  const isNoneLocalName = resolveImportLocalName(j, root, 'isNone', '@ember/utils');

  // Transform CallExpressions
  root.find(j.CallExpression).forEach((path) => {
    const node = path.node;

    // #12: model.hasMany('rel').value() → model.rel
    const hasManyVal = isHasManyValue(j, node);
    if (hasManyVal) {
      j(path).replaceWith(
        j.memberExpression(hasManyVal.receiver, j.identifier(hasManyVal.relName)),
      );
      changed = true;
      return;
    }

    // #14: model.belongsTo('rel').value() → model.rel
    const belongsToVal = isBelongsToValue(j, node);
    if (belongsToVal) {
      j(path).replaceWith(
        j.memberExpression(belongsToVal.receiver, j.identifier(belongsToVal.relName)),
      );
      changed = true;
      return;
    }

    // #13: model.hasMany('rel').ids() → (model.rel || []).map(r => r.id)
    const hasManyId = isHasManyIds(j, node);
    if (hasManyId) {
      const relAccess = j.memberExpression(
        hasManyId.receiver,
        j.identifier(hasManyId.relName),
      );
      const fallback = j.logicalExpression('||', relAccess, j.arrayExpression([]));
      const mapCall = j.callExpression(
        j.memberExpression(
          j.parenthesizedExpression(fallback),
          j.identifier('map'),
        ),
        [
          j.arrowFunctionExpression(
            [j.identifier('r')],
            j.memberExpression(j.identifier('r'), j.identifier('id')),
          ),
        ],
      );
      j(path).replaceWith(mapCall);
      changed = true;
      return;
    }

    // #1 & #2: this.get('prop') → this.prop / this.nested?.path
    const thisGet = isThisGet(j, node);
    if (thisGet) {
      const { propPath } = thisGet;
      if (propPath.includes('.')) {
        const parts = propPath.split('.');
        // Skip transform if any segment is not a valid identifier
        if (parts.some((p) => !isValidIdentifier(p))) return;
        j(path).replaceWith(buildOptionalChain(j, parts));
      } else {
        // buildThisProp handles invalid identifiers via computed access
        j(path).replaceWith(buildThisProp(j, propPath));
      }
      changed = true;
      return;
    }

    // #3c: obj.get('prop') → obj.prop / obj.a?.b (non-this receiver)
    const objGet = isObjectGet(j, node);
    if (objGet) {
      const { receiver, propPath } = objGet;
      if (propPath.includes('.')) {
        const parts = propPath.split('.');
        // Skip transform if any segment is not a valid identifier
        if (parts.some((p) => !isValidIdentifier(p))) return;
        j(path).replaceWith(buildOptionalChainOnReceiver(j, receiver, parts));
      } else if (isValidIdentifier(propPath)) {
        j(path).replaceWith(
          j.memberExpression(receiver, j.identifier(propPath)),
        );
      } else {
        // Use computed member expression for invalid identifiers: obj['some-prop']
        j(path).replaceWith(
          j.memberExpression(receiver, j.stringLiteral(propPath), true),
        );
      }
      changed = true;
      return;
    }

    // #3d: get(obj, 'prop') → obj.prop (functional form from @ember/object)
    if (hasEmberGetImport) {
      const funcGet = isEmberGetFunctional(j, node, getLocalName!);
      if (funcGet) {
        const { obj, propPath } = funcGet;
        if (propPath.includes('.')) {
          const parts = propPath.split('.');
          // Skip transform if any segment is not a valid identifier
          if (parts.some((p) => !isValidIdentifier(p))) return;
          j(path).replaceWith(buildOptionalChainOnReceiver(j, obj, parts));
        } else if (isValidIdentifier(propPath)) {
          j(path).replaceWith(
            j.memberExpression(obj, j.identifier(propPath)),
          );
        } else {
          // Use computed member expression for invalid identifiers: obj['some-prop']
          j(path).replaceWith(
            j.memberExpression(obj, j.stringLiteral(propPath), true),
          );
        }
        changed = true;
        return;
      }
    }

    // #3: this.set('prop', val) → this.prop = val
    const thisSet = isThisSet(j, node);
    if (thisSet) {
      const left = isValidIdentifier(thisSet.propName)
        ? j.memberExpression(j.thisExpression(), j.identifier(thisSet.propName))
        : j.memberExpression(j.thisExpression(), j.stringLiteral(thisSet.propName), true);
      const assignment = j.assignmentExpression(
        '=',
        left,
        thisSet.valueNode,
      );
      if (j.ExpressionStatement.check(path.parent.node)) {
        j(path.parent).replaceWith(j.expressionStatement(assignment));
      } else {
        j(path).replaceWith(assignment);
      }
      changed = true;
      return;
    }

    // #3f: obj.set('prop', val) → obj.prop = val (non-this receiver)
    const objSet = isObjectSet(j, node);
    if (objSet) {
      const left = isValidIdentifier(objSet.propName)
        ? j.memberExpression(objSet.receiver, j.identifier(objSet.propName))
        : j.memberExpression(objSet.receiver, j.stringLiteral(objSet.propName), true);
      const assignment = j.assignmentExpression(
        '=',
        left,
        objSet.valueNode,
      );
      if (j.ExpressionStatement.check(path.parent.node)) {
        j(path.parent).replaceWith(j.expressionStatement(assignment));
      } else {
        j(path).replaceWith(assignment);
      }
      changed = true;
      return;
    }

    // #3b: this.set(`${type}Time`, val) → this[`${type}Time`] = val
    const thisSetTpl = isThisSetTemplateLiteral(j, node);
    if (thisSetTpl) {
      const assignment = j.assignmentExpression(
        '=',
        j.memberExpression(j.thisExpression(), thisSetTpl.keyNode, true), // computed
        thisSetTpl.valueNode,
      );
      if (j.ExpressionStatement.check(path.parent.node)) {
        j(path.parent).replaceWith(j.expressionStatement(assignment));
      } else {
        j(path).replaceWith(assignment);
      }
      changed = true;
      return;
    }

    // #3e: set(obj, 'prop', val) → obj.prop = val (functional form from @ember/object)
    if (hasEmberSetImport) {
      const funcSet = isEmberSetFunctional(j, node, setLocalName!);
      if (funcSet) {
        const left = isValidIdentifier(funcSet.propName)
          ? j.memberExpression(funcSet.obj, j.identifier(funcSet.propName))
          : j.memberExpression(funcSet.obj, j.stringLiteral(funcSet.propName), true);
        const assignment = j.assignmentExpression(
          '=',
          left,
          funcSet.valueNode,
        );
        if (j.ExpressionStatement.check(path.parent.node)) {
          j(path.parent).replaceWith(j.expressionStatement(assignment));
        } else {
          j(path).replaceWith(assignment);
        }
        changed = true;
        return;
      }
    }

    // #4: .toArray() → Array.from(...)
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

    // #5: .sortBy('key') → .sort((a, b) => a.key < b.key ? -1 : a.key > b.key ? 1 : 0)
    // Also handles multi-key: .sortBy('k1', 'k2') → chained comparator
    const sortByMatch = isSortBy(j, node);
    if (sortByMatch) {
      // Skip if any key is not a valid identifier (would produce broken member expressions)
      if (sortByMatch.keys.some((k) => !isValidIdentifier(k))) return;
      const comparator = sortByMatch.keys.length === 1
        ? buildSortComparator(j, sortByMatch.key)
        : buildMultiKeySortComparator(j, sortByMatch.keys);
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

    // #6: .mapBy('key') → .map(item => item.key)
    const mapByMatch = isMapBy(j, node);
    if (mapByMatch) {
      // Skip if key is not a valid identifier (would produce broken member expressions)
      if (!isValidIdentifier(mapByMatch.key)) return;
      const mapCall = j.callExpression(
        j.memberExpression(mapByMatch.receiver, j.identifier('map')),
        [
          j.arrowFunctionExpression(
            [j.identifier('item')],
            j.memberExpression(j.identifier('item'), j.identifier(mapByMatch.key)),
          ),
        ],
      );
      j(path).replaceWith(mapCall);
      changed = true;
      return;
    }

    // #7: .filterBy('key', val?) → .filter(item => item.key === val)
    // NOTE: Ember's filterBy uses loose equality (==) for the 2-arg form,
    // but we emit strict equality (===) which is safer and more predictable.
    // The single-arg form (truthy check) is semantically identical.
    // A trailing comment is added to the 2-arg output to flag the difference for manual audit.
    const filterByMatch = isFilterBy(j, node);
    if (filterByMatch) {
      // Skip if key is not a valid identifier (would produce broken member expressions)
      if (!isValidIdentifier(filterByMatch.key)) return;
      let filterBody;
      const itemKey = j.memberExpression(
        j.identifier('item'),
        j.identifier(filterByMatch.key),
      );

      if (filterByMatch.value) {
        filterBody = j.binaryExpression('===', itemKey, filterByMatch.value);
      } else {
        filterBody = itemKey;
      }

      // Add trailing comment for 2-arg form to flag loose vs strict equality difference
      if (filterByMatch.value) {
        (filterBody as any).comments = (filterBody as any).comments || [];
        (filterBody as any).comments.push(
          j.commentLine(' NOTE: Ember filterBy used == (loose equality)', false, true),
        );
      }

      const filterCall = j.callExpression(
        j.memberExpression(filterByMatch.receiver, j.identifier('filter')),
        [j.arrowFunctionExpression([j.identifier('item')], filterBody)],
      );
      j(path).replaceWith(filterCall);
      changed = true;
      return;
    }

    // #8: .uniq() → [...new Set(...)]
    const uniqMatch = isUniq(j, node);
    if (uniqMatch) {
      j(path).replaceWith(
        j.arrayExpression([
          j.spreadElement(
            j.newExpression(j.identifier('Set'), [uniqMatch.receiver]),
          ),
        ]),
      );
      changed = true;
      return;
    }

    // #15: .findBy('key', val) → .find(item => item.key === val)
    // NOTE: Like filterBy, Ember's findBy uses loose equality (==) for the 2-arg form.
    // We emit strict equality (===). See filterBy comment above for rationale.
    const findByMatch = isFindBy(j, node);
    if (findByMatch) {
      // Skip if key is not a valid identifier (would produce broken member expressions)
      if (!isValidIdentifier(findByMatch.key)) return;
      const itemKey = j.memberExpression(
        j.identifier('item'),
        j.identifier(findByMatch.key),
      );

      let findBody;
      if (findByMatch.value) {
        findBody = j.binaryExpression('===', itemKey, findByMatch.value);
      } else {
        findBody = itemKey;
      }

      // Add trailing comment for 2-arg form to flag loose vs strict equality difference
      if (findByMatch.value) {
        (findBody as any).comments = (findBody as any).comments || [];
        (findBody as any).comments.push(
          j.commentLine(' NOTE: Ember findBy used == (loose equality)', false, true),
        );
      }

      const findCall = j.callExpression(
        j.memberExpression(findByMatch.receiver, j.identifier('find')),
        [j.arrowFunctionExpression([j.identifier('item')], findBody)],
      );
      j(path).replaceWith(findCall);
      changed = true;
      return;
    }

    // #16: .pushObject(item) → .push(item)
    // When return value is used: (arr.push(item), item) to preserve semantics
    // (pushObject returns the item, push returns the new length)
    const pushObjectMatch = isPushObject(j, node);
    if (pushObjectMatch) {
      const pushCall = j.callExpression(
        j.memberExpression(pushObjectMatch.receiver, j.identifier('push')),
        [pushObjectMatch.item],
      );
      if (j.ExpressionStatement.check(path.parent.node)) {
        // Statement context: simple replacement
        j(path).replaceWith(pushCall);
      } else {
        // Expression context: (arr.push(item), item) to return the item
        j(path).replaceWith(
          j.sequenceExpression([pushCall, cloneNode(pushObjectMatch.item)]),
        );
      }
      changed = true;
      return;
    }

    // #16b: .pushObjects(items) → .push(...items)
    // When return value is used: (arr.push(...items), arr) since pushObjects returns the array
    if (
      j.MemberExpression.check(node.callee) &&
      j.Identifier.check(node.callee.property) &&
      node.callee.property.name === 'pushObjects' &&
      node.arguments.length === 1
    ) {
      const receiver = node.callee.object;
      const pushCall = j.callExpression(
        j.memberExpression(receiver, j.identifier('push')),
        [j.spreadElement(node.arguments[0] as any)],
      );
      if (j.ExpressionStatement.check(path.parent.node)) {
        // Statement context: simple replacement
        j(path).replaceWith(pushCall);
      } else {
        // Expression context: (arr.push(...items), arr) to return the array
        j(path).replaceWith(
          j.sequenceExpression([pushCall, cloneNode(receiver)]),
        );
      }
      changed = true;
      return;
    }

    // #17b: .removeObjects(items) → items.forEach(item => { const _idx = arr.indexOf(item); if (_idx !== -1) arr.splice(_idx, 1); })
    if (
      j.MemberExpression.check(node.callee) &&
      j.Identifier.check(node.callee.property) &&
      node.callee.property.name === 'removeObjects' &&
      node.arguments.length === 1
    ) {
      const receiver = node.callee.object;
      const itemsArg = node.arguments[0] as any;

      // Determine whether receiver is simple or needs hoisting
      const simple = isSimpleExpression(j, receiver);
      const arrRef = simple ? receiver : j.identifier('_arr');

      const idxId = j.identifier('_idx');
      const indexOfCall = j.callExpression(
        j.memberExpression(arrRef, j.identifier('indexOf')),
        [j.identifier('_item')],
      );
      const spliceCall = j.expressionStatement(
        j.callExpression(
          j.memberExpression(arrRef, j.identifier('splice')),
          [idxId, j.numericLiteral(1)],
        ),
      );
      const ifStmt = j.ifStatement(
        j.binaryExpression('!==', idxId, j.unaryExpression('-', j.numericLiteral(1))),
        spliceCall,
      );
      const forEachBody = j.blockStatement([
        j.variableDeclaration('const', [
          j.variableDeclarator(idxId, indexOfCall),
        ]),
        ifStmt,
      ]);
      const forEachCallback = j.arrowFunctionExpression(
        [j.identifier('_item')],
        forEachBody,
      );
      const forEachCall = j.callExpression(
        j.memberExpression(itemsArg, j.identifier('forEach')),
        [forEachCallback],
      );

      if (j.ExpressionStatement.check(path.parent.node)) {
        // Statement context
        if (!simple) {
          // Need to hoist receiver
          const block = j.blockStatement([
            j.variableDeclaration('const', [
              j.variableDeclarator(j.identifier('_arr'), receiver),
            ]),
            j.expressionStatement(forEachCall),
          ]);
          j(path.parent).replaceWith(block);
        } else {
          j(path).replaceWith(forEachCall);
        }
      } else {
        // Expression context: IIFE returning the array
        const iifeStmts: any[] = [];
        if (!simple) {
          iifeStmts.push(
            j.variableDeclaration('const', [
              j.variableDeclarator(j.identifier('_arr'), receiver),
            ]),
          );
        }
        iifeStmts.push(j.expressionStatement(forEachCall));
        iifeStmts.push(j.returnStatement(arrRef));
        j(path).replaceWith(
          j.callExpression(
            j.arrowFunctionExpression([], j.blockStatement(iifeStmts)),
            [],
          ),
        );
      }
      changed = true;
      return;
    }

    // #17: .removeObject(item) → safe splice-based removal
    // Guard: indexOf can return -1, and splice(-1, 1) would remove the LAST element.
    // So we generate: { const _i = arr.indexOf(item); if (_i !== -1) arr.splice(_i, 1); }
    const removeObjectMatch = isRemoveObject(j, node);
    if (removeObjectMatch) {
      const receiver = removeObjectMatch.receiver;
      const idxId = j.identifier('_idx');

      // Determine whether receiver is simple or needs hoisting
      const simple = isSimpleExpression(j, receiver);
      const arrRef = simple ? receiver : j.identifier('_arr');

      const stmts: any[] = [];

      // If receiver is not simple, hoist to temp variable
      if (!simple) {
        stmts.push(
          j.variableDeclaration('const', [
            j.variableDeclarator(j.identifier('_arr'), receiver),
          ]),
        );
      }

      const indexOfCall = j.callExpression(
        j.memberExpression(arrRef, j.identifier('indexOf')),
        [removeObjectMatch.item],
      );
      stmts.push(
        j.variableDeclaration('const', [
          j.variableDeclarator(idxId, indexOfCall),
        ]),
      );
      const spliceCall = j.expressionStatement(
        j.callExpression(
          j.memberExpression(arrRef, j.identifier('splice')),
          [idxId, j.numericLiteral(1)],
        ),
      );
      const ifStmt = j.ifStatement(
        j.binaryExpression('!==', idxId, j.unaryExpression('-', j.numericLiteral(1))),
        spliceCall,
      );
      stmts.push(ifStmt);
      const block = j.blockStatement(stmts);

      // Replace the parent ExpressionStatement with a block
      if (j.ExpressionStatement.check(path.parent.node)) {
        j(path.parent).replaceWith(block);
      } else {
        // Expression context: IIFE that returns the array (preserving removeObject semantics)
        const iifeStmts = [...cloneNode(stmts), j.returnStatement(cloneNode(arrRef))];
        const iifeBlock = j.blockStatement(iifeStmts);
        j(path).replaceWith(
          j.callExpression(
            j.arrowFunctionExpression(
              [],
              iifeBlock,
            ),
            [],
          ),
        );
      }
      changed = true;
      return;
    }

    // #18: hasMany('rel').load() → remove (no-op when async: false)
    if (
      j.MemberExpression.check(node.callee) &&
      j.Identifier.check(node.callee.property) &&
      node.callee.property.name === 'load' &&
      node.arguments.length === 0 &&
      j.CallExpression.check(node.callee.object)
    ) {
      const inner = node.callee.object;
      if (
        j.MemberExpression.check(inner.callee) &&
        j.Identifier.check(inner.callee.property) &&
        (inner.callee.property.name === 'hasMany' || inner.callee.property.name === 'belongsTo') &&
        inner.arguments.length === 1 &&
        j.StringLiteral.check(inner.arguments[0])
      ) {
        // Replace with just accessing the relationship property
        const relName = (inner.arguments[0] as any).value;
        j(path).replaceWith(
          j.memberExpression(inner.callee.object, j.identifier(relName)),
        );
        changed = true;
        return;
      }
    }

    // #9: isEmpty(x) → x == null || x === '' || (Array.isArray(x) && x.length === 0)
    const isEmptyMatch = isEmptyLocalName ? isEmberIsEmpty(j, node, isEmptyLocalName) : null;
    if (isEmptyMatch) {
      if (isEmptyLocalName) {
        const arg = isEmptyMatch.arg;

        // Build the isEmpty logical expression using a given reference identifier
        const buildIsEmptyExpr = (ref: any) => {
          const expr = j.logicalExpression(
            '||',
            j.logicalExpression(
              '||',
              j.binaryExpression('==', ref, j.nullLiteral()),
              j.binaryExpression('===', ref, j.stringLiteral('')),
            ),
            j.logicalExpression(
              '&&',
              j.callExpression(
                j.memberExpression(j.identifier('Array'), j.identifier('isArray')),
                [ref],
              ),
              j.binaryExpression(
                '===',
                j.memberExpression(ref, j.identifier('length')),
                j.numericLiteral(0),
              ),
            ),
          );
          // Add TODO comment about incomplete isEmpty semantics
          (expr as any).comments = (expr as any).comments || [];
          (expr as any).comments.push(
            j.commentLine(' TODO: Ember\'s isEmpty also returns true for NaN and non-array objects with length === 0', false, true),
          );
          return expr;
        };

        if (isSimpleExpression(j, arg)) {
          // Safe to inline — arg has no side effects
          j(path).replaceWith(buildIsEmptyExpr(arg));
        } else {
          // Wrap in IIFE to evaluate arg only once:
          // ((x) => x == null || x === '' || (Array.isArray(x) && x.length === 0))(arg)
          const paramId = j.identifier('x');
          j(path).replaceWith(
            j.callExpression(
              j.arrowFunctionExpression(
                [paramId],
                buildIsEmptyExpr(j.identifier('x')),
              ),
              [arg],
            ),
          );
        }
        removeImport(j, root, 'isEmpty', '@ember/utils');
        changed = true;
      }
      return;
    }

    // #9b: isPresent(x) → x != null && x !== '' && (!Array.isArray(x) || x.length > 0)
    const isPresentMatch = isPresentLocalName ? isEmberIsPresent(j, node, isPresentLocalName) : null;
    if (isPresentMatch) {
      if (isPresentLocalName) {
        const arg = isPresentMatch.arg;

        // Build the isPresent logical expression using a given reference identifier
        const buildIsPresentExpr = (ref: any) =>
          j.logicalExpression(
            '&&',
            j.logicalExpression(
              '&&',
              j.binaryExpression('!=', ref, j.nullLiteral()),
              j.binaryExpression('!==', ref, j.stringLiteral('')),
            ),
            j.logicalExpression(
              '||',
              j.unaryExpression(
                '!',
                j.callExpression(
                  j.memberExpression(j.identifier('Array'), j.identifier('isArray')),
                  [ref],
                ),
              ),
              j.binaryExpression(
                '>',
                j.memberExpression(ref, j.identifier('length')),
                j.numericLiteral(0),
              ),
            ),
          );

        if (isSimpleExpression(j, arg)) {
          // Safe to inline — arg has no side effects
          j(path).replaceWith(buildIsPresentExpr(arg));
        } else {
          // Wrap in IIFE to evaluate arg only once:
          // ((x) => x != null && x !== '' && (!Array.isArray(x) || x.length > 0))(arg)
          const paramId = j.identifier('x');
          j(path).replaceWith(
            j.callExpression(
              j.arrowFunctionExpression(
                [paramId],
                buildIsPresentExpr(j.identifier('x')),
              ),
              [arg],
            ),
          );
        }
        removeImport(j, root, 'isPresent', '@ember/utils');
        changed = true;
      }
      return;
    }

    // #9c: isNone(x) → x == null
    const isNoneMatch = isNoneLocalName ? isEmberIsNone(j, node, isNoneLocalName) : null;
    if (isNoneMatch) {
      if (isNoneLocalName) {
        j(path).replaceWith(
          j.binaryExpression('==', isNoneMatch.arg, j.nullLiteral()),
        );
        removeImport(j, root, 'isNone', '@ember/utils');
        changed = true;
      }
      return;
    }

    // #20: A(arr) → arr, A() → [] (from @ember/array)
    if (hasEmberA && j.Identifier.check(node.callee) && node.callee.name === aLocalName) {
      if (node.arguments.length === 1) {
        // A(arr) → arr
        j(path).replaceWith(node.arguments[0]);
      } else if (node.arguments.length === 0) {
        // A() → []
        j(path).replaceWith(j.arrayExpression([]));
      } else {
        // A(x, y, ...) — not a standard usage, skip
        return;
      }
      changed = true;
      return;
    }

    // #21: isArray(x) → Array.isArray(x) (from @ember/array)
    if (hasEmberIsArray && j.Identifier.check(node.callee) && node.callee.name === isArrayLocalName && node.arguments.length === 1) {
      j(path).replaceWith(
        j.callExpression(
          j.memberExpression(j.identifier('Array'), j.identifier('isArray')),
          [node.arguments[0]],
        ),
      );
      changed = true;
      return;
    }

    // #22: obj.setProperties({ key: val, ... }) → obj.key = val; ...
    // Also handles this.setProperties({...})
    const setPropertiesMatch = isSetProperties(j, node);
    if (setPropertiesMatch) {
      const receiver = setPropertiesMatch.receiver;
      const props = setPropertiesMatch.properties.properties;

      // Only transform if all properties have string/identifier keys (bail on computed/spread)
      const allSimple = props.every(
        (p: any) => p.type === 'ObjectProperty' && !p.computed && (j.Identifier.check(p.key) || j.StringLiteral.check(p.key)),
      );
      if (allSimple && props.length > 0) {
        // When receiver is not simple (e.g. getModel()), hoist to temp variable
        // to avoid evaluating it multiple times (once per property).
        const needsHoist = !isSimpleExpression(j, receiver) && props.length > 1;
        const assignRef = needsHoist ? j.identifier('_obj') : receiver;

        const assignments: any[] = [];

        if (needsHoist) {
          assignments.push(
            j.variableDeclaration('const', [
              j.variableDeclarator(j.identifier('_obj'), receiver),
            ]),
          );
        }

        props.forEach((p: any) => {
          const key = j.Identifier.check(p.key) ? p.key.name : p.key.value;
          assignments.push(
            j.expressionStatement(
              j.assignmentExpression(
                '=',
                j.memberExpression(assignRef, j.identifier(key)),
                p.value,
              ),
            ),
          );
        });

        if (j.ExpressionStatement.check(path.parent.node)) {
          if (needsHoist) {
            // Wrap in block to scope the temp variable
            j(path.parent).replaceWith(j.blockStatement(assignments));
          } else {
            // Replace the parent ExpressionStatement with multiple statements
            j(path.parent).replaceWith(assignments);
          }
        } else {
          // Expression context: wrap in IIFE that returns the properties object
          // (Ember's setProperties returns the passed properties hash)
          const iifeBody = [
            ...assignments,
            j.returnStatement(cloneNode(setPropertiesMatch.properties)),
          ];
          j(path).replaceWith(
            j.callExpression(
              j.arrowFunctionExpression([], j.blockStatement(iifeBody)),
              [],
            ),
          );
        }
        changed = true;
        return;
      }
    }

    // #10: this.transitionTo(...) → this.router.transitionTo(...)
    // Only inside Route or Controller classes
    const transitionMatch = isThisTransitionTo(j, node);
    if (transitionMatch) {
      const enclosingClass = findEnclosingRouteOrController(j, path, root);
      if (enclosingClass) {
        j(path).replaceWith(
          j.callExpression(
            j.memberExpression(
              j.memberExpression(j.thisExpression(), j.identifier('router')),
              j.identifier('transitionTo'),
            ),
            transitionMatch.args,
          ),
        );
        ensureRouterService(j, root, enclosingClass);
        changed = true;
        return;
      }
    }

    // #11: this.replaceWith(...) → this.router.replaceWith(...)
    // Only inside Route or Controller classes
    const replaceMatch = isThisReplaceWith(j, node);
    if (replaceMatch) {
      const enclosingClass = findEnclosingRouteOrController(j, path, root);
      if (enclosingClass) {
        j(path).replaceWith(
          j.callExpression(
            j.memberExpression(
              j.memberExpression(j.thisExpression(), j.identifier('router')),
              j.identifier('replaceWith'),
            ),
            replaceMatch.args,
          ),
        );
        ensureRouterService(j, root, enclosingClass);
        changed = true;
        return;
      }
    }
  });

  // #19: .firstObject → [0], .lastObject → .at(-1)
  root.find(j.MemberExpression).forEach((path) => {
    const node = path.node;
    if (!j.Identifier.check(node.property)) return;

    if (node.property.name === 'firstObject') {
      j(path).replaceWith(
        j.memberExpression(node.object, j.numericLiteral(0), true),
      );
      changed = true;
    } else if (node.property.name === 'lastObject') {
      j(path).replaceWith(
        j.callExpression(
          j.memberExpression(node.object, j.identifier('at')),
          [j.unaryExpression('-', j.numericLiteral(1))],
        ),
      );
      changed = true;
    }
  });

  // Clean up functional get/set imports from @ember/object if all usages were transformed
  if (changed && hasEmberGetImport) {
    const remainingGetCalls = root
      .find(j.CallExpression)
      .filter((p) => isEmberGetFunctional(j, p.node, getLocalName!) !== null);
    if (remainingGetCalls.length === 0) {
      removeImport(j, root, 'get', '@ember/object');
    }
  }
  if (changed && hasEmberSetImport) {
    const remainingSetCalls = root
      .find(j.CallExpression)
      .filter((p) => isEmberSetFunctional(j, p.node, setLocalName!) !== null);
    if (remainingSetCalls.length === 0) {
      removeImport(j, root, 'set', '@ember/object');
    }
  }

  // Clean up A/isArray imports from @ember/array if all usages were transformed
  if (changed && hasEmberA) {
    const remainingACalls = root
      .find(j.CallExpression)
      .filter((p) => j.Identifier.check(p.node.callee) && p.node.callee.name === aLocalName);
    if (remainingACalls.length === 0) {
      removeImport(j, root, 'A', '@ember/array');
    }
  }
  if (changed && hasEmberIsArray) {
    const remainingIsArrayCalls = root
      .find(j.CallExpression)
      .filter((p) => j.Identifier.check(p.node.callee) && p.node.callee.name === isArrayLocalName);
    if (remainingIsArrayCalls.length === 0) {
      removeImport(j, root, 'isArray', '@ember/array');
    }
  }

  if (!changed) return undefined;
  return root.toSource({ quote: 'single' });
}

/**
 * Resolve the local name for an imported specifier.
 * For `import { get as emberGet } from '@ember/object'`, calling
 * resolveImportLocalName(j, root, 'get', '@ember/object') returns 'emberGet'.
 * If the specifier is not aliased, returns the imported name itself.
 * Returns null if the specifier is not found.
 */
function resolveImportLocalName(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
  importedName: string,
  source: string,
): string | null {
  let localName: string | null = null;
  root
    .find(j.ImportDeclaration, { source: { value: source } })
    .forEach((importPath) => {
      (importPath.node.specifiers ?? []).forEach((s) => {
        if (j.ImportSpecifier.check(s) && s.imported.name === importedName) {
          localName = s.local?.name ?? s.imported.name;
        }
      });
    });
  return localName;
}

/** Names that indicate a Route or Controller superclass. */
const ROUTE_CONTROLLER_SUPERCLASSES = new Set([
  'Route', 'Controller',
]);

/** Import sources that indicate Route or Controller. */
const ROUTE_CONTROLLER_IMPORT_SOURCES = new Set([
  '@ember/routing/route',
  '@ember/controller',
]);

/**
 * Find the enclosing Route or Controller class for a given AST path.
 * Walks up the tree to find an enclosing ClassDeclaration or ClassExpression
 * and checks:
 *  1. The superClass name matches Route/Controller
 *  2. OR the file has imports from Route/Controller sources
 *
 * Returns the class node if found, or null if the path is not inside a Route/Controller.
 */
function findEnclosingRouteOrController(
  j: API['jscodeshift'],
  nodePath: any,
  root: ReturnType<API['jscodeshift']>,
): any | null {
  // Walk up to find enclosing class
  let current = nodePath;
  while (current) {
    const node = current.node;
    if (j.ClassDeclaration.check(node) || j.ClassExpression.check(node)) {
      // Check the superclass name
      if (node.superClass) {
        if (j.Identifier.check(node.superClass) && ROUTE_CONTROLLER_SUPERCLASSES.has(node.superClass.name)) {
          return node;
        }
        // Handle MemberExpression like Ember.Route
        if (j.MemberExpression.check(node.superClass) && j.Identifier.check(node.superClass.property)) {
          if (ROUTE_CONTROLLER_SUPERCLASSES.has(node.superClass.property.name)) {
            return node;
          }
        }
      }
      // Fall through: check file-level imports
      break;
    }
    current = current.parent;
  }

  // If we found a class but couldn't confirm from superclass name,
  // check if file has relevant Route/Controller imports
  if (current) {
    const hasImport = root
      .find(j.ImportDeclaration)
      .some((importPath) => {
        const source = importPath.node.source.value;
        return typeof source === 'string' && ROUTE_CONTROLLER_IMPORT_SOURCES.has(source);
      });
    return hasImport ? current.node : null;
  }

  // Not inside any class at all — skip
  return null;
}

/**
 * Ensure `@service router` declaration exists in the specific class
 * that triggered the transform, and ensure RouterService import exists.
 *
 * @param classNode - The specific ClassDeclaration or ClassExpression node to inject into.
 */
function ensureRouterService(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
  classNode: any,
): void {
  const body = classNode.body.body;
  const hasRouterService = body.some((member: any) => {
    if (!j.ClassProperty.check(member)) return false;
    const decorators = (member as any).decorators;
    if (!Array.isArray(decorators)) return false;
    return decorators.some((d: any) => {
      const name =
        d.expression?.name ??
        d.expression?.callee?.name;
      return name === 'service';
    }) && (member.key as any)?.name === 'router';
  });

  if (!hasRouterService) {
    const routerProp = j.classProperty(
      j.identifier('router'),
      null,
    ) as any;
    routerProp.declare = true;
    routerProp.decorators = [
      {
        type: 'Decorator',
        expression: j.identifier('service'),
      },
    ];
    routerProp.typeAnnotation = j.tsTypeAnnotation(
      j.tsTypeReference(j.identifier('RouterService')),
    );
    body.unshift(routerProp);
  }

  // #23 Fix: Ensure RouterService type is imported
  addImport(j, root, 'RouterService', '@ember/routing', { isType: true });

  // Ensure @service decorator is imported
  addImport(j, root, 'service', '@ember/service');
}

export default withGtsSupport(transformer);
