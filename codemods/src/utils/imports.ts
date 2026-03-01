import type { API, ASTPath, FileInfo, ImportDeclaration } from 'jscodeshift';

/**
 * Replace the source of an import declaration.
 * e.g. replaceImportSource(j, root, '@ember-data/model', '@warp-drive/legacy/model')
 */
export function replaceImportSource(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
  oldSource: string,
  newSource: string,
): boolean {
  let changed = false;
  root
    .find(j.ImportDeclaration, { source: { value: oldSource } })
    .forEach((path) => {
      path.node.source = j.literal(newSource);
      changed = true;
    });
  return changed;
}

/**
 * Add an import declaration if one doesn't already exist for that source/specifier combo.
 */
export function addImport(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
  specifier: string,
  source: string,
  options?: { isType?: boolean; isDefault?: boolean },
): void {
  const { isType = false, isDefault = false } = options ?? {};

  // Check if this import already exists
  const existing = root.find(j.ImportDeclaration, {
    source: { value: source },
  });

  if (existing.length > 0) {
    // M15: Check ALL import declarations from the same source (not just the first).
    // There may be multiple imports from the same source (e.g., one type, one value).
    // First, check if the specifier already exists in ANY of them.
    let alreadyImported = false;
    existing.forEach((path) => {
      const specs = path.node.specifiers ?? [];
      if (specs.some((s) => {
        if (isDefault) {
          return j.ImportDefaultSpecifier.check(s);
        }
        return (
          j.ImportSpecifier.check(s) &&
          ((s as any).imported.name === specifier || s.local?.name === specifier)
        );
      })) {
        alreadyImported = true;
      }
    });
    if (alreadyImported) return;

    // Find a compatible declaration to merge into (matching importKind).
    let compatibleDecl: ImportDeclaration | null = null;
    existing.forEach((path) => {
      if (compatibleDecl) return;
      const node = path.node;
      const nodeIsType = node.importKind === 'type';
      if (isType === nodeIsType) {
        compatibleDecl = node;
      }
    });

    if (compatibleDecl) {
      // Merge into the compatible declaration
      const specs = (compatibleDecl as ImportDeclaration).specifiers ?? [];
      if (isDefault) {
        specs.unshift(j.importDefaultSpecifier(j.identifier(specifier)));
      } else {
        specs.push(j.importSpecifier(j.identifier(specifier)));
      }
      return;
    }

    // No compatible declaration — create a new one with the right importKind.
    const spec = isDefault
      ? j.importDefaultSpecifier(j.identifier(specifier))
      : j.importSpecifier(j.identifier(specifier));
    const newDecl = j.importDeclaration([spec], j.literal(source));
    if (isType) {
      newDecl.importKind = 'type';
    }
    existing.at(-1).insertAfter(newDecl);
    return;
  }

  // Create new import declaration
  let spec;
  if (isDefault) {
    spec = j.importDefaultSpecifier(j.identifier(specifier));
  } else {
    spec = j.importSpecifier(j.identifier(specifier));
  }

  const decl = j.importDeclaration([spec], j.literal(source));
  if (isType) {
    decl.importKind = 'type';
  }

  // Insert after the last import
  const allImports = root.find(j.ImportDeclaration);
  if (allImports.length > 0) {
    allImports.at(-1).insertAfter(decl);
  } else {
    // No imports at all — insert at beginning of program body
    const body = root.find(j.Program).get().node.body;
    body.unshift(decl);
  }
}

/**
 * Remove an import specifier. If the import declaration becomes empty, remove it entirely.
 * Returns true if something was removed.
 */
export function removeImport(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
  specifier: string,
  source?: string,
): boolean {
  let changed = false;

  const filter: Record<string, unknown> = {};
  if (source) {
    filter.source = { value: source };
  }

  root.find(j.ImportDeclaration, filter).forEach((path) => {
    const specs = path.node.specifiers ?? [];
    const idx = specs.findIndex((s) => {
      if (j.ImportDefaultSpecifier.check(s)) {
        return s.local?.name === specifier;
      }
      if (j.ImportSpecifier.check(s)) {
        return (
          s.imported.name === specifier || s.local?.name === specifier
        );
      }
      return false;
    });

    if (idx !== -1) {
      specs.splice(idx, 1);
      changed = true;
      if (specs.length === 0) {
        j(path).remove();
      }
    }
  });

  return changed;
}

/**
 * Remove an entire import declaration by source.
 */
export function removeImportBySource(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
  source: string,
): boolean {
  const imports = root.find(j.ImportDeclaration, {
    source: { value: source },
  });
  if (imports.length > 0) {
    imports.remove();
    return true;
  }
  return false;
}

/**
 * Convert a value import to a type-only import.
 */
export function convertToTypeImport(
  j: API['jscodeshift'],
  path: ASTPath<ImportDeclaration>,
): void {
  path.node.importKind = 'type';
}

/**
 * Check if a given identifier is only used in type positions within the file.
 * A usage is "type-only" if it appears inside a TSTypeAnnotation, TSTypeReference,
 * TSTypeAliasDeclaration, TSInterfaceDeclaration, or as the type side of TSAsExpression.
 */
export function isUsedOnlyAsType(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
  identifierName: string,
): boolean {
  let usedAsValue = false;

  root.find(j.Identifier, { name: identifierName }).forEach((path) => {
    if (usedAsValue) return; // short-circuit

    // Skip the import declaration itself
    if (isInsideImportDeclaration(path)) return;

    // Walk up from this identifier through ancestors.
    // If we reach a type-only AST context before reaching a value context, it's type-only.
    if (isInTypeContext(j, path)) return;

    usedAsValue = true;
  });

  return !usedAsValue;
}

function isInsideImportDeclaration(path: ASTPath): boolean {
  let current = path.parent;
  while (current) {
    if (current.node.type === 'ImportDeclaration') return true;
    current = current.parent;
  }
  return false;
}

/**
 * Walk ancestors from an Identifier to determine if it's in a type-only context.
 */
function isInTypeContext(j: API['jscodeshift'], path: ASTPath): boolean {
  let current: ASTPath | undefined = path;
  while (current?.parent) {
    const parentNode = current.parent.node;

    // Definite type contexts
    if (
      j.TSTypeAnnotation.check(parentNode) ||
      j.TSTypeReference.check(parentNode) ||
      j.TSTypeParameterInstantiation.check(parentNode) ||
      j.TSQualifiedName.check(parentNode) ||
      j.TSTypeAliasDeclaration.check(parentNode) ||
      j.TSInterfaceDeclaration.check(parentNode) ||
      j.TSUnionType?.check(parentNode) ||
      j.TSIntersectionType?.check(parentNode) ||
      j.TSArrayType?.check(parentNode) ||
      j.TSTypeParameterDeclaration?.check(parentNode) ||
      j.TSMappedType?.check(parentNode) ||
      j.TSConditionalType?.check(parentNode) ||
      j.TSInferType?.check(parentNode) ||
      j.TSIndexedAccessType?.check(parentNode)
    ) {
      return true;
    }

    // TSAsExpression: only the typeAnnotation side is a type context.
    // `expr as Type` — expr is value, Type is type.
    // We check which child property of TSAsExpression our path goes through.
    if (j.TSAsExpression.check(parentNode)) {
      // current.name is the property name on the parent (e.g., 'expression' or 'typeAnnotation')
      const propName = current.name;
      if (propName === 'typeAnnotation') return true;
      // 'expression' side — this is a value usage, keep walking
    }

    // ExportNamedDeclaration with type-only export
    if (
      j.ExportNamedDeclaration.check(parentNode) &&
      (parentNode as any).exportKind === 'type'
    ) {
      return true;
    }

    current = current.parent;
  }
  return false;
}
