import type { API, ASTPath, CallExpression } from 'jscodeshift';

/**
 * Check if a string is a valid JavaScript identifier.
 * Used to decide between `obj.prop` (dot access) and `obj['prop']` (computed access).
 */
export function isValidIdentifier(name: string): boolean {
  return /^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(name);
}

/**
 * Check if a CallExpression is `this.get('propName')` or `this.get('nested.path')`
 */
export function isThisGet(
  j: API['jscodeshift'],
  node: CallExpression,
): { propPath: string } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.ThisExpression.check(node.callee.object) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'get' &&
    node.arguments.length === 1 &&
    j.StringLiteral.check(node.arguments[0])
  ) {
    return { propPath: (node.arguments[0] as any).value };
  }
  return null;
}

/**
 * Receiver names that have their own `.get()` method unrelated to Ember.
 * We skip transforming these to avoid false positives.
 */
const NON_EMBER_GET_RECEIVERS = new Set([
  'searchparams',
  'params',
  'headers',
  'map',
  'formdata',
  'urlsearchparams',
  'cookies',
  'cache',
  'storage',
  'localstorage',
  'sessionstorage',
]);

/**
 * Check if a CallExpression is `obj.get('propName')` where obj is NOT `this`.
 * Handles: someObj.get('prop'), this.parent.get('prop'), etc.
 *
 * Guards against false positives: skips Map, URLSearchParams, Headers, etc.
 * that have their own `.get()` method.
 */
export function isObjectGet(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; propPath: string } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    !j.ThisExpression.check(node.callee.object) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'get' &&
    node.arguments.length === 1 &&
    j.StringLiteral.check(node.arguments[0])
  ) {
    // Guard: skip known non-Ember receivers that have their own .get() method
    const receiver = node.callee.object;

    // Direct identifier check: map.get('key'), searchParams.get('q')
    if (j.Identifier.check(receiver) && NON_EMBER_GET_RECEIVERS.has(receiver.name.toLowerCase())) {
      return null;
    }

    // Member expression check: this.searchParams.get('q'), url.searchParams.get('q')
    if (j.MemberExpression.check(receiver) && j.Identifier.check(receiver.property)) {
      if (NON_EMBER_GET_RECEIVERS.has(receiver.property.name.toLowerCase())) {
        return null;
      }
    }

    // Guard: skip if receiver is a `new` expression (new Map(), new URLSearchParams(), etc.)
    if (j.NewExpression.check(receiver)) {
      return null;
    }

    return {
      receiver,
      propPath: (node.arguments[0] as any).value,
    };
  }
  return null;
}

/**
 * Check if a CallExpression is the functional form: `get(obj, 'propName')`
 * imported from '@ember/object'.
 * Accepts an optional localName to handle aliased imports like
 * `import { get as emberGet } from '@ember/object'`.
 */
export function isEmberGetFunctional(
  j: API['jscodeshift'],
  node: CallExpression,
  localName: string = 'get',
): { obj: any; propPath: string } | null {
  if (
    j.Identifier.check(node.callee) &&
    node.callee.name === localName &&
    node.arguments.length === 2 &&
    j.StringLiteral.check(node.arguments[1])
  ) {
    return {
      obj: node.arguments[0],
      propPath: (node.arguments[1] as any).value,
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `obj.set('propName', value)` where obj is NOT `this`.
 * Handles: model.set('name', val), this.parent.set('active', true), etc.
 *
 * Guards against false positives: skips Map, Headers, etc.
 */
export function isObjectSet(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; propName: string; valueNode: any } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    !j.ThisExpression.check(node.callee.object) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'set' &&
    node.arguments.length === 2 &&
    j.StringLiteral.check(node.arguments[0])
  ) {
    const receiver = node.callee.object;

    // Guard: skip known non-Ember receivers
    if (j.Identifier.check(receiver) && NON_EMBER_GET_RECEIVERS.has(receiver.name.toLowerCase())) {
      return null;
    }
    if (j.MemberExpression.check(receiver) && j.Identifier.check(receiver.property)) {
      if (NON_EMBER_GET_RECEIVERS.has(receiver.property.name.toLowerCase())) {
        return null;
      }
    }
    if (j.NewExpression.check(receiver)) {
      return null;
    }

    return {
      receiver,
      propName: (node.arguments[0] as any).value,
      valueNode: node.arguments[1],
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `this.set('propName', value)` with string literal key
 */
export function isThisSet(
  j: API['jscodeshift'],
  node: CallExpression,
): { propName: string; valueNode: any } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.ThisExpression.check(node.callee.object) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'set' &&
    node.arguments.length === 2 &&
    j.StringLiteral.check(node.arguments[0])
  ) {
    return {
      propName: (node.arguments[0] as any).value,
      valueNode: node.arguments[1],
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `this.set(templateLiteral, value)` with template literal key.
 * e.g. this.set(`${type}Time`, new Date())
 */
export function isThisSetTemplateLiteral(
  j: API['jscodeshift'],
  node: CallExpression,
): { keyNode: any; valueNode: any } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.ThisExpression.check(node.callee.object) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'set' &&
    node.arguments.length === 2 &&
    j.TemplateLiteral.check(node.arguments[0])
  ) {
    return {
      keyNode: node.arguments[0],
      valueNode: node.arguments[1],
    };
  }
  return null;
}

/**
 * Check if a CallExpression is the functional form: `set(obj, 'propName', value)`
 * imported from '@ember/object'.
 * Accepts an optional localName to handle aliased imports like
 * `import { set as emberSet } from '@ember/object'`.
 */
export function isEmberSetFunctional(
  j: API['jscodeshift'],
  node: CallExpression,
  localName: string = 'set',
): { obj: any; propName: string; valueNode: any } | null {
  if (
    j.Identifier.check(node.callee) &&
    node.callee.name === localName &&
    node.arguments.length === 3 &&
    j.StringLiteral.check(node.arguments[1])
  ) {
    return {
      obj: node.arguments[0],
      propName: (node.arguments[1] as any).value,
      valueNode: node.arguments[2],
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `.toArray()` (0 args).
 *
 * We skip when the receiver is a `new` expression (e.g. `new Uint8Array(...).toArray()`)
 * to avoid false positives with TypedArrays and other non-Ember objects.
 * We intentionally do NOT add broader guards (like the ones on `.get()` for Map, Headers,
 * etc.) because `Array.from()` works correctly on any iterable/array-like, so the
 * replacement is semantically safe even for non-Ember receivers.
 */
export function isToArray(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'toArray' &&
    node.arguments.length === 0
  ) {
    const receiver = node.callee.object;

    // Guard: skip if receiver is a `new` expression (new Uint8Array(), new Immutable.List(), etc.)
    if (j.NewExpression.check(receiver)) {
      return null;
    }

    return { receiver };
  }
  return null;
}

/**
 * Check if a CallExpression is `.sortBy('key')` or `.sortBy('key1', 'key2', ...)`
 */
export function isSortBy(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; key: string; keys: string[] } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'sortBy' &&
    node.arguments.length >= 1 &&
    node.arguments.every((arg) => j.StringLiteral.check(arg))
  ) {
    return {
      receiver: node.callee.object,
      key: (node.arguments[0] as any).value,
      keys: node.arguments.map((arg: any) => arg.value),
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `.mapBy('key')`
 */
export function isMapBy(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; key: string } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'mapBy' &&
    node.arguments.length === 1 &&
    j.StringLiteral.check(node.arguments[0])
  ) {
    return {
      receiver: node.callee.object,
      key: (node.arguments[0] as any).value,
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `.filterBy('key')` or `.filterBy('key', val)`
 */
export function isFilterBy(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; key: string; value: any | null } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'filterBy' &&
    (node.arguments.length === 1 || node.arguments.length === 2) &&
    j.StringLiteral.check(node.arguments[0])
  ) {
    return {
      receiver: node.callee.object,
      key: (node.arguments[0] as any).value,
      value: node.arguments.length === 2 ? node.arguments[1] : null,
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `.uniq()` (0 args)
 */
export function isUniq(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'uniq' &&
    node.arguments.length === 0
  ) {
    return { receiver: node.callee.object };
  }
  return null;
}

/**
 * Check if a CallExpression is `isEmpty(x)` from @ember/utils.
 * Accepts an optional localName to handle aliased imports like
 * `import { isEmpty as emberIsEmpty } from '@ember/utils'`.
 */
export function isEmberIsEmpty(
  j: API['jscodeshift'],
  node: CallExpression,
  localName: string = 'isEmpty',
): { arg: any } | null {
  if (
    j.Identifier.check(node.callee) &&
    node.callee.name === localName &&
    node.arguments.length === 1
  ) {
    return { arg: node.arguments[0] };
  }
  return null;
}

/**
 * Check if a CallExpression is `isPresent(x)` from @ember/utils.
 * Accepts an optional localName to handle aliased imports like
 * `import { isPresent as emberIsPresent } from '@ember/utils'`.
 */
export function isEmberIsPresent(
  j: API['jscodeshift'],
  node: CallExpression,
  localName: string = 'isPresent',
): { arg: any } | null {
  if (
    j.Identifier.check(node.callee) &&
    node.callee.name === localName &&
    node.arguments.length === 1
  ) {
    return { arg: node.arguments[0] };
  }
  return null;
}

/**
 * Check if a CallExpression is `isNone(x)` from @ember/utils.
 * Accepts an optional localName to handle aliased imports like
 * `import { isNone as emberIsNone } from '@ember/utils'`.
 */
export function isEmberIsNone(
  j: API['jscodeshift'],
  node: CallExpression,
  localName: string = 'isNone',
): { arg: any } | null {
  if (
    j.Identifier.check(node.callee) &&
    node.callee.name === localName &&
    node.arguments.length === 1
  ) {
    return { arg: node.arguments[0] };
  }
  return null;
}

/**
 * Check if a CallExpression is `this.transitionTo(...)`
 */
export function isThisTransitionTo(
  j: API['jscodeshift'],
  node: CallExpression,
): { args: any[] } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.ThisExpression.check(node.callee.object) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'transitionTo'
  ) {
    return { args: node.arguments };
  }
  return null;
}

/**
 * Check if a CallExpression is `this.replaceWith(...)`
 */
export function isThisReplaceWith(
  j: API['jscodeshift'],
  node: CallExpression,
): { args: any[] } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.ThisExpression.check(node.callee.object) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'replaceWith'
  ) {
    return { args: node.arguments };
  }
  return null;
}

/**
 * Check if a CallExpression is `model.hasMany('rel').value()`
 * Pattern: expr.hasMany(str).value()
 */
export function isHasManyValue(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; relName: string } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'value' &&
    node.arguments.length === 0 &&
    j.CallExpression.check(node.callee.object)
  ) {
    const inner = node.callee.object;
    if (
      j.MemberExpression.check(inner.callee) &&
      j.Identifier.check(inner.callee.property) &&
      inner.callee.property.name === 'hasMany' &&
      inner.arguments.length === 1 &&
      j.StringLiteral.check(inner.arguments[0])
    ) {
      return {
        receiver: inner.callee.object,
        relName: (inner.arguments[0] as any).value,
      };
    }
  }
  return null;
}

/**
 * Check if a CallExpression is `model.hasMany('rel').ids()`
 * Pattern: expr.hasMany(str).ids()
 */
export function isHasManyIds(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; relName: string } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'ids' &&
    node.arguments.length === 0 &&
    j.CallExpression.check(node.callee.object)
  ) {
    const inner = node.callee.object;
    if (
      j.MemberExpression.check(inner.callee) &&
      j.Identifier.check(inner.callee.property) &&
      inner.callee.property.name === 'hasMany' &&
      inner.arguments.length === 1 &&
      j.StringLiteral.check(inner.arguments[0])
    ) {
      return {
        receiver: inner.callee.object,
        relName: (inner.arguments[0] as any).value,
      };
    }
  }
  return null;
}

/**
 * Check if a CallExpression is `model.belongsTo('rel').value()`
 */
export function isBelongsToValue(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; relName: string } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'value' &&
    node.arguments.length === 0 &&
    j.CallExpression.check(node.callee.object)
  ) {
    const inner = node.callee.object;
    if (
      j.MemberExpression.check(inner.callee) &&
      j.Identifier.check(inner.callee.property) &&
      inner.callee.property.name === 'belongsTo' &&
      inner.arguments.length === 1 &&
      j.StringLiteral.check(inner.arguments[0])
    ) {
      return {
        receiver: inner.callee.object,
        relName: (inner.arguments[0] as any).value,
      };
    }
  }
  return null;
}

/**
 * Check if a CallExpression is `.findBy('key', val?)` — Ember array findBy
 */
export function isFindBy(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; key: string; value: any | null } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'findBy' &&
    (node.arguments.length === 1 || node.arguments.length === 2) &&
    j.StringLiteral.check(node.arguments[0])
  ) {
    return {
      receiver: node.callee.object,
      key: (node.arguments[0] as any).value,
      value: node.arguments.length === 2 ? node.arguments[1] : null,
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `.pushObject(item)` — Ember array pushObject
 */
export function isPushObject(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; item: any } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'pushObject' &&
    node.arguments.length === 1
  ) {
    return {
      receiver: node.callee.object,
      item: node.arguments[0],
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `.removeObject(item)` — Ember array removeObject
 */
export function isRemoveObject(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; item: any } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'removeObject' &&
    node.arguments.length === 1
  ) {
    return {
      receiver: node.callee.object,
      item: node.arguments[0],
    };
  }
  return null;
}

/**
 * Check if a CallExpression is `.setProperties({...})` — Ember setProperties
 */
export function isSetProperties(
  j: API['jscodeshift'],
  node: CallExpression,
): { receiver: any; properties: any } | null {
  if (
    j.MemberExpression.check(node.callee) &&
    j.Identifier.check(node.callee.property) &&
    node.callee.property.name === 'setProperties' &&
    node.arguments.length === 1 &&
    j.ObjectExpression.check(node.arguments[0])
  ) {
    return {
      receiver: node.callee.object,
      properties: node.arguments[0],
    };
  }
  return null;
}

/**
 * Check if an AST node is a "simple" expression that can be safely duplicated
 * without causing multiple evaluations (side effects).
 * Returns true for: Identifiers, ThisExpression, and MemberExpressions
 * where all parts are themselves simple.
 */
export function isSimpleExpression(
  j: API['jscodeshift'],
  node: any,
): boolean {
  if (j.Identifier.check(node)) return true;
  if (j.ThisExpression.check(node)) return true;
  if (j.MemberExpression.check(node)) {
    // The object part must be simple
    if (!isSimpleExpression(j, node.object)) return false;
    // If it's computed (e.g., obj[expr]), the property must also be simple
    if (node.computed) {
      return isSimpleExpression(j, node.property);
    }
    // Non-computed (obj.prop) — property is always an Identifier, which is simple
    return true;
  }
  return false;
}

/**
 * Build a `this.propName` or `this['propName']` member expression.
 * Uses computed access when propName is not a valid JS identifier.
 */
export function buildThisProp(
  j: API['jscodeshift'],
  propName: string,
): any {
  if (isValidIdentifier(propName)) {
    return j.memberExpression(j.thisExpression(), j.identifier(propName));
  }
  return j.memberExpression(j.thisExpression(), j.stringLiteral(propName), true);
}

/**
 * Build optional chaining for nested paths: `this.a?.b?.c`
 */
export function buildOptionalChain(
  j: API['jscodeshift'],
  parts: string[],
): any {
  let expr: any = j.thisExpression();
  for (let i = 0; i < parts.length; i++) {
    if (i === 0) {
      expr = j.memberExpression(expr, j.identifier(parts[i]));
    } else {
      expr = j.optionalMemberExpression(
        expr,
        j.identifier(parts[i]),
        false,
        true,
      );
    }
  }
  return expr;
}

/**
 * Build optional chaining for nested paths on an arbitrary receiver: `receiver.a?.b?.c`
 */
export function buildOptionalChainOnReceiver(
  j: API['jscodeshift'],
  receiver: any,
  parts: string[],
): any {
  let expr: any = receiver;
  for (let i = 0; i < parts.length; i++) {
    if (i === 0) {
      expr = j.memberExpression(expr, j.identifier(parts[i]));
    } else {
      expr = j.optionalMemberExpression(
        expr,
        j.identifier(parts[i]),
        false,
        true,
      );
    }
  }
  return expr;
}

/**
 * Build a locale-safe sort comparator: (a, b) => a.key < b.key ? -1 : a.key > b.key ? 1 : 0
 */
export function buildSortComparator(
  j: API['jscodeshift'],
  key: string,
): any {
  const aKey = j.memberExpression(j.identifier('a'), j.identifier(key));
  const bKey = j.memberExpression(j.identifier('b'), j.identifier(key));

  // a.key < b.key ? -1 : a.key > b.key ? 1 : 0
  return j.arrowFunctionExpression(
    [j.identifier('a'), j.identifier('b')],
    j.conditionalExpression(
      j.binaryExpression('<', aKey, bKey),
      j.unaryExpression('-', j.numericLiteral(1)),
      j.conditionalExpression(
        j.binaryExpression(
          '>',
          j.memberExpression(j.identifier('a'), j.identifier(key)),
          j.memberExpression(j.identifier('b'), j.identifier(key)),
        ),
        j.numericLiteral(1),
        j.numericLiteral(0),
      ),
    ),
  );
}

/**
 * Build a chained sort comparator for multiple keys:
 * (a, b) => a.k1 < b.k1 ? -1 : a.k1 > b.k1 ? 1 : a.k2 < b.k2 ? -1 : a.k2 > b.k2 ? 1 : 0
 */
export function buildMultiKeySortComparator(
  j: API['jscodeshift'],
  keys: string[],
): any {
  // Build from the last key backwards
  // The innermost fallback is 0
  let expr: any = j.numericLiteral(0);

  for (let i = keys.length - 1; i >= 0; i--) {
    const key = keys[i];
    const aKey = j.memberExpression(j.identifier('a'), j.identifier(key));
    const bKey = j.memberExpression(j.identifier('b'), j.identifier(key));

    expr = j.conditionalExpression(
      j.binaryExpression('<', aKey, bKey),
      j.unaryExpression('-', j.numericLiteral(1)),
      j.conditionalExpression(
        j.binaryExpression(
          '>',
          j.memberExpression(j.identifier('a'), j.identifier(key)),
          j.memberExpression(j.identifier('b'), j.identifier(key)),
        ),
        j.numericLiteral(1),
        expr,
      ),
    );
  }

  return j.arrowFunctionExpression(
    [j.identifier('a'), j.identifier('b')],
    expr,
  );
}
