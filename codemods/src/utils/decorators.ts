import type { API, ASTPath, ClassBody, ClassDeclaration, ClassProperty, ClassMethod } from 'jscodeshift';

type DecoratorNode = {
  type: 'Decorator';
  expression: any;
};

/**
 * Get decorators from a ClassProperty or ClassMethod node.
 * Uses direct property access to work around jscodeshift bug #469
 * where root.find(j.Decorator) misses decorators on ClassProperty nodes.
 */
export function getDecorators(
  node: ClassProperty | ClassMethod | ClassDeclaration,
): DecoratorNode[] {
  // Access decorators directly — they exist on the node but jscodeshift
  // types don't always expose them
  const decorators = (node as any).decorators;
  if (!Array.isArray(decorators)) return [];
  return decorators;
}

/**
 * Get the name of a decorator.
 * Handles both simple decorators (@foo) and call expressions (@foo(...))
 */
export function getDecoratorName(decorator: DecoratorNode): string | null {
  const expr = decorator.expression;
  if (!expr) return null;

  // @foo
  if (expr.type === 'Identifier') {
    return expr.name;
  }

  // @foo.bar (MemberExpression without call)
  if (expr.type === 'MemberExpression') {
    return expr.property?.name ?? null;
  }

  // @foo(...)
  if (expr.type === 'CallExpression') {
    if (expr.callee?.type === 'Identifier') {
      return expr.callee.name;
    }
    // @foo.bar(...)
    if (expr.callee?.type === 'MemberExpression') {
      return expr.callee.property?.name ?? null;
    }
  }

  return null;
}

/**
 * Get arguments of a decorator call expression.
 * Returns empty array for non-call decorators.
 */
export function getDecoratorArgs(decorator: DecoratorNode): any[] {
  const expr = decorator.expression;
  if (expr?.type === 'CallExpression') {
    return expr.arguments ?? [];
  }
  return [];
}

/**
 * Check if a class member has a specific decorator.
 */
export function hasDecorator(
  node: ClassProperty | ClassMethod | ClassDeclaration,
  name: string,
): boolean {
  return getDecorators(node).some((d) => getDecoratorName(d) === name);
}

/**
 * Find the first decorator with a given name on a node.
 */
export function findDecorator(
  node: ClassProperty | ClassMethod | ClassDeclaration,
  name: string,
): DecoratorNode | undefined {
  return getDecorators(node).find((d) => getDecoratorName(d) === name);
}

/**
 * Remove a decorator from a node by name.
 */
export function removeDecorator(
  node: ClassProperty | ClassMethod | ClassDeclaration,
  name: string,
): boolean {
  const decorators = (node as any).decorators;
  if (!Array.isArray(decorators)) return false;

  const idx = decorators.findIndex(
    (d: DecoratorNode) => getDecoratorName(d) === name,
  );
  if (idx !== -1) {
    decorators.splice(idx, 1);
    return true;
  }
  return false;
}

/**
 * Classify a class member by its decorators and type.
 * Returns a structured description of the member.
 */
export type MemberClassification =
  | { kind: 'attribute'; name: string; attrType: string | null; options: any | null; tsType: string | null }
  | { kind: 'belongsTo'; name: string; relatedType: string; options: any }
  | { kind: 'hasMany'; name: string; relatedType: string; options: any }
  | { kind: 'tracked'; name: string; defaultValue: any; tsType: string | null }
  | { kind: 'service'; name: string; serviceName: string }
  | { kind: 'getter'; name: string; body: string; isCached: boolean }
  | { kind: 'setter'; name: string; body: string }
  | { kind: 'method'; name: string; body: string; isAction: boolean }
  | { kind: 'property'; name: string; body: string; tsType: string | null }
  | { kind: 'declare'; name: string; tsType: string | null }
  | { kind: 'unknown'; name: string };

export function classifyMember(
  j: API['jscodeshift'],
  member: any,
  sourceCode: string,
): MemberClassification {
  // Handle both regular identifiers and private names (#prop)
  const name = member.key?.type === 'PrivateName'
    ? `#${member.key.id?.name ?? ''}`
    : (member.key?.name ?? member.key?.value ?? '');

  // Static members don't belong in WarpDrive extensions — skip them
  if (member.static === true) {
    return { kind: 'unknown', name };
  }

  if (j.ClassProperty.check(member)) {
    const decorators = getDecorators(member);

    // @attr
    const attrDec = decorators.find((d) => getDecoratorName(d) === 'attr');
    if (attrDec) {
      const args = getDecoratorArgs(attrDec);
      const attrType =
        args.length > 0 && args[0]?.type === 'StringLiteral'
          ? args[0].value
          : null;
      const options = args.length > 1 ? extractSourceText(args[1], sourceCode) : null;
      const tsType = extractTSType(member.typeAnnotation, sourceCode);
      return { kind: 'attribute', name, attrType, options, tsType };
    }

    // @belongsTo
    const btDec = decorators.find((d) => getDecoratorName(d) === 'belongsTo');
    if (btDec) {
      const args = getDecoratorArgs(btDec);
      const relatedType = args[0]?.value ?? '';
      const options = args[1] ?? {};
      return { kind: 'belongsTo', name, relatedType, options };
    }

    // @hasMany
    const hmDec = decorators.find((d) => getDecoratorName(d) === 'hasMany');
    if (hmDec) {
      const args = getDecoratorArgs(hmDec);
      const relatedType = args[0]?.value ?? '';
      const options = args[1] ?? {};
      return { kind: 'hasMany', name, relatedType, options };
    }

    // @service
    const svcDec = decorators.find((d) => getDecoratorName(d) === 'service');
    if (svcDec) {
      const args = getDecoratorArgs(svcDec);
      const serviceName = args[0]?.value ?? name;
      return { kind: 'service', name, serviceName };
    }

    // @tracked
    const trackedDec = decorators.find(
      (d) => getDecoratorName(d) === 'tracked',
    );
    if (trackedDec) {
      const defaultValue = member.value
        ? extractSourceText(member.value, sourceCode)
        : null;
      const tsType = extractTSType(member.typeAnnotation, sourceCode);
      return { kind: 'tracked', name, defaultValue, tsType };
    }

    // declare property (no value, no decorator)
    if ((member as any).declare) {
      const tsType = extractTSType(member.typeAnnotation, sourceCode);
      return { kind: 'declare', name, tsType };
    }

    // Plain property with value
    if (member.value) {
      const body = extractSourceText(member.value, sourceCode);
      const tsType = extractTSType(member.typeAnnotation, sourceCode);
      return { kind: 'property', name, body, tsType };
    }

    return { kind: 'unknown', name };
  }

  if (j.ClassMethod.check(member) || j.TSDeclareMethod?.check(member)) {
    if (member.kind === 'constructor') {
      return { kind: 'unknown', name: 'constructor' };
    }

    const decorators = getDecorators(member as any);
    const body = extractMethodBody(member, sourceCode);

    if (member.kind === 'get') {
      const isCached = decorators.some((d) => getDecoratorName(d) === 'cached');
      return { kind: 'getter', name, body, isCached };
    }

    if (member.kind === 'set') {
      return { kind: 'setter', name, body };
    }

    const isAction = decorators.some((d) => getDecoratorName(d) === 'action');
    return { kind: 'method', name, body, isAction };
  }

  return { kind: 'unknown', name };
}

function extractSourceText(node: any, sourceCode: string): string {
  if (node.start != null && node.end != null) {
    return sourceCode.slice(node.start, node.end);
  }
  return '';
}

function extractMethodBody(node: any, sourceCode: string): string {
  if (node.body?.start != null && node.body?.end != null) {
    // Get the body block including braces
    return sourceCode.slice(node.body.start, node.body.end);
  }
  return '{ /* TODO */ }';
}

function extractTSType(
  typeAnnotation: any,
  sourceCode: string,
): string | null {
  if (!typeAnnotation) return null;
  // The typeAnnotation is a TSTypeAnnotation wrapping the actual type
  const inner = typeAnnotation.typeAnnotation ?? typeAnnotation;
  if (inner.start != null && inner.end != null) {
    let result = sourceCode.slice(inner.start, inner.end);
    // Trim leading colon from type annotation syntax (e.g. ": string" → "string")
    if (result.startsWith(':')) {
      result = result.slice(1).trimStart();
    }
    return result;
  }
  return null;
}
