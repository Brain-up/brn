import type { API, FileInfo, Options } from 'jscodeshift';
import { replaceImportSource, addImport, removeImport } from './utils/imports';
import { getDecorators, getDecoratorName, getDecoratorArgs } from './utils/decorators';
import { withGtsSupport } from './utils/gts-support';

/**
 * Phase 1: Import Migration
 *
 * Rewrite ember-data imports to WarpDrive, add [Type] brand, fix relationships.
 * Transformations:
 *  1.  @ember-data/model → @warp-drive/legacy/model
 *  2.  @ember-data/store → APP_NAME/services/store (as type import)
 *  3.  @ember-data/adapter/* → @warp-drive/legacy/adapter/*
 *  4.  @ember-data/serializer/* → @warp-drive/legacy/serializer/*
 *  5.  Remove ember-data/types/registries/* module declarations
 *  6.  Add [Type] brand to Model classes
 *  7.  Add inverse: null to relationships missing inverse
 *  8.  Convert async: true → async: false on hasMany AND belongsTo
 *  9.  SyncHasMany<X> → X[]
 *  10. AsyncHasMany<X> → X[]
 *  11. AsyncBelongsTo<X> → X
 *  12. @ember-data/model/-private → remove (after extracting type specifiers)
 */

interface Phase1Options {
  appName?: string;
}

const DEFAULT_IMPORT_MAP: Record<string, string> = {
  '@ember-data/model': '@warp-drive/legacy/model',
  '@ember-data/model/-private': '@warp-drive/legacy/model',
  '@ember-data/adapter': '@warp-drive/legacy/adapter',
  '@ember-data/adapter/rest': '@warp-drive/legacy/adapter/rest',
  '@ember-data/adapter/json-api': '@warp-drive/legacy/adapter/json-api',
  '@ember-data/serializer': '@warp-drive/legacy/serializer',
  '@ember-data/serializer/json': '@warp-drive/legacy/serializer/json',
  '@ember-data/serializer/json-api': '@warp-drive/legacy/serializer/json-api',
  '@ember-data/serializer/rest': '@warp-drive/legacy/serializer/rest',
  '@ember-data/serializer/transform': '@warp-drive/legacy/serializer/transform',
  '@ember-data/debug': '@warp-drive/legacy/debug',
};

// Mapping from DS.X property names to their correct WarpDrive package and import type
const DS_MEMBER_MAP: Record<string, { source: string; isDefault: boolean }> = {
  Model: { source: '@warp-drive/legacy/model', isDefault: true },
  attr: { source: '@warp-drive/legacy/model', isDefault: false },
  belongsTo: { source: '@warp-drive/legacy/model', isDefault: false },
  hasMany: { source: '@warp-drive/legacy/model', isDefault: false },
  RESTAdapter: { source: '@warp-drive/legacy/adapter/rest', isDefault: true },
  JSONAPIAdapter: { source: '@warp-drive/legacy/adapter/json-api', isDefault: true },
  Adapter: { source: '@warp-drive/legacy/adapter', isDefault: true },
  RESTSerializer: { source: '@warp-drive/legacy/serializer/rest', isDefault: true },
  JSONSerializer: { source: '@warp-drive/legacy/serializer/json', isDefault: true },
  JSONAPISerializer: { source: '@warp-drive/legacy/serializer/json-api', isDefault: true },
  Serializer: { source: '@warp-drive/legacy/serializer', isDefault: true },
  Transform: { source: '@warp-drive/legacy/serializer/transform', isDefault: true },
};

// Type specifiers that should be removed (not re-exported from WarpDrive)
const REMOVED_TYPE_SPECIFIERS = new Set([
  'SyncHasMany',
  'AsyncHasMany',
  'AsyncBelongsTo',
]);

function transformer(
  fileInfo: FileInfo,
  api: API,
  options: Options,
): string | undefined {
  const j = api.jscodeshift;
  const root = j(fileInfo.source);
  let changed = false;

  const phase1Opts: Phase1Options = options as any;
  const appName = phase1Opts.appName ?? 'app';

  // --- 1-4: Import source replacements ---

  for (const [oldSource, newSource] of Object.entries(DEFAULT_IMPORT_MAP)) {
    // For -private imports, we only keep non-removed specifiers
    if (oldSource === '@ember-data/model/-private') {
      root
        .find(j.ImportDeclaration, { source: { value: oldSource } })
        .forEach((path) => {
          const specs = path.node.specifiers ?? [];
          // Remove type specifiers that we handle separately (SyncHasMany, etc.)
          const remaining = specs.filter((s) => {
            if (j.ImportSpecifier.check(s)) {
              return !REMOVED_TYPE_SPECIFIERS.has(s.imported.name);
            }
            return true;
          });
          if (remaining.length === 0) {
            j(path).remove();
          } else {
            path.node.specifiers = remaining;
            path.node.source = j.literal(newSource);
          }
          changed = true;
        });
      continue;
    }

    if (replaceImportSource(j, root, oldSource, newSource)) {
      changed = true;
    }
  }

  // Handle legacy barrel import: `import DS from 'ember-data'`
  root
    .find(j.ImportDeclaration, { source: { value: 'ember-data' } })
    .forEach((path) => {
      const specs = path.node.specifiers ?? [];

      // L5: Handle named imports: `import { Model, attr } from 'ember-data'`
      const namedSpecs = specs.filter((s) => j.ImportSpecifier.check(s));
      if (namedSpecs.length > 0 && !specs.some((s) => j.ImportDefaultSpecifier.check(s) || j.ImportNamespaceSpecifier.check(s))) {
        for (const spec of namedSpecs) {
          if (j.ImportSpecifier.check(spec)) {
            const importedName = spec.imported.name;
            const localName = spec.local?.name ?? importedName;
            const mapping = DS_MEMBER_MAP[importedName];
            if (mapping) {
              addImport(j, root, mapping.isDefault ? localName : importedName, mapping.source, {
                isDefault: mapping.isDefault,
              });
              // If the local name differs from imported and it's not a default, we need to handle aliasing
              // For named imports, if localName !== importedName, the addImport added imported name;
              // we need to find and rename the specifier
            } else {
              // Unknown member — add to legacy/model with TODO
              addImport(j, root, localName, '@warp-drive/legacy/model');
              root.find(j.ImportDeclaration, { source: { value: '@warp-drive/legacy/model' } })
                .forEach((importPath) => {
                  const importSpecs = importPath.node.specifiers ?? [];
                  const hasSpec = importSpecs.some((s: any) =>
                    (s.local?.name === localName) || (s.imported?.name === localName),
                  );
                  if (hasSpec) {
                    importPath.node.comments = importPath.node.comments ?? [];
                    const alreadyHasTodo = importPath.node.comments.some(
                      (c: any) => c.value?.includes(`ember-data.${importedName}`),
                    );
                    if (!alreadyHasTodo) {
                      importPath.node.comments.push(
                        j.commentLine(` TODO: ember-data.${importedName} — verify correct @warp-drive import`, true),
                      );
                    }
                  }
                });
            }
          }
        }
        j(path).remove();
        changed = true;
        return; // skip further processing for this import
      }

      // L5: Handle namespace import: `import * as DS from 'ember-data'`
      // Treat same as default import — the DS.X member access handler below already works
      const namespaceSpec = specs.find((s) => j.ImportNamespaceSpecifier.check(s));
      const defaultSpec = specs.find((s) => j.ImportDefaultSpecifier.check(s));

      if (namespaceSpec || defaultSpec) {
        const localName = (namespaceSpec ?? defaultSpec)?.local?.name ?? 'DS';

        // H6 Fix: Handle destructured usage: `const { X, Y } = DS;`
        root
          .find(j.VariableDeclarator)
          .filter((vdPath) => {
            const init = vdPath.node.init;
            return j.Identifier.check(init) && init.name === localName;
          })
          .forEach((vdPath) => {
            const id = vdPath.node.id;
            if (j.ObjectPattern.check(id)) {
              for (const prop of id.properties) {
                if (
                  j.ObjectProperty?.check(prop) ||
                  prop.type === 'ObjectProperty' ||
                  prop.type === 'Property'
                ) {
                  const key = (prop as any).key?.name ?? (prop as any).value?.name;
                  const localPropName = (prop as any).value?.name ?? key;
                  if (key) {
                    const mapping = DS_MEMBER_MAP[key];
                    if (mapping) {
                      addImport(j, root, localPropName, mapping.source, { isDefault: mapping.isDefault });
                    } else {
                      // Unknown DS member — add import with TODO comment
                      addImport(j, root, localPropName, '@warp-drive/legacy/model');
                      // Find the import we just added and attach a TODO comment
                      root.find(j.ImportDeclaration, { source: { value: '@warp-drive/legacy/model' } })
                        .forEach((importPath) => {
                          const specs = importPath.node.specifiers ?? [];
                          const hasSpec = specs.some((s: any) =>
                            (s.local?.name === localPropName) || (s.imported?.name === localPropName),
                          );
                          if (hasSpec) {
                            importPath.node.comments = importPath.node.comments ?? [];
                            const alreadyHasTodo = importPath.node.comments.some(
                              (c: any) => c.value?.includes(`DS.${key}`),
                            );
                            if (!alreadyHasTodo) {
                              importPath.node.comments.push(
                                j.commentLine(` TODO: DS.${key} — verify correct @warp-drive import`, true),
                              );
                            }
                          }
                        });
                    }
                  }
                }
              }
              // Remove the destructuring statement
              const varDecl = vdPath.parent;
              if (varDecl?.node?.declarations?.length === 1) {
                j(varDecl).remove();
              }
            }
          });

        // H6 Fix: Handle member access: `DS.X` patterns
        root
          .find(j.MemberExpression, {
            object: { type: 'Identifier', name: localName },
          })
          .forEach((mePath) => {
            const property = mePath.node.property;
            if (j.Identifier.check(property) && !mePath.node.computed) {
              const memberName = property.name;
              const mapping = DS_MEMBER_MAP[memberName];
              if (mapping) {
                // Add the correct import
                addImport(j, root, memberName, mapping.source, { isDefault: mapping.isDefault });
                // Replace DS.X with just X
                j(mePath).replaceWith(j.identifier(memberName));
              } else {
                // Unknown DS member — leave member expression as-is, add TODO comment
                const stmtPath = findParentStatement(mePath);
                if (stmtPath) {
                  const node = stmtPath.node;
                  node.comments = node.comments ?? [];
                  node.comments.unshift(j.commentLine(` TODO: DS.${memberName} — verify correct @warp-drive import`));
                }
              }
            }
          });

        // Remove the original `import DS from 'ember-data'` / `import * as DS from 'ember-data'`
        j(path).remove();
        changed = true;
      }
    });

  // Special case: @ember-data/store → APP_NAME/services/store (type import)
  // H7 Fix: Only the default import (Store) becomes a type import.
  // Named value imports stay as value imports from '@ember-data/store' (with TODO).
  root
    .find(j.ImportDeclaration, { source: { value: '@ember-data/store' } })
    .forEach((path) => {
      const specs = path.node.specifiers ?? [];
      const defaultSpecs = specs.filter((s) => j.ImportDefaultSpecifier.check(s));
      const namedSpecs = specs.filter((s) => j.ImportSpecifier.check(s));

      if (defaultSpecs.length > 0 && namedSpecs.length > 0) {
        // Split into two declarations:
        // 1. type import for default (Store)
        const typeDecl = j.importDeclaration(
          defaultSpecs,
          j.literal(`${appName}/services/store`),
        );
        typeDecl.importKind = 'type';

        // 2. value import for named exports with TODO comment
        const namedDecl = j.importDeclaration(
          namedSpecs,
          j.literal('@ember-data/store'),
        );
        namedDecl.comments = [
          j.commentLine(' TODO: verify @ember-data/store named imports exist in @warp-drive'),
        ];

        j(path).replaceWith([typeDecl, namedDecl]);
        changed = true;
      } else if (defaultSpecs.length > 0) {
        // Only default import — convert to type import
        path.node.source = j.literal(`${appName}/services/store`);
        path.node.importKind = 'type';
        changed = true;
      } else if (namedSpecs.length > 0) {
        // Only named imports — keep as value import with TODO
        path.node.comments = path.node.comments ?? [];
        path.node.comments.unshift(
          j.commentLine(' TODO: verify @ember-data/store named imports exist in @warp-drive'),
        );
        changed = true;
      }
    });

  // --- 5: Remove ember-data/types/registries module declarations ---

  root.find(j.TSModuleDeclaration).forEach((path) => {
    const id = path.node.id;
    if (
      j.StringLiteral.check(id) &&
      (id as any).value?.startsWith('ember-data/types/registries')
    ) {
      j(path).remove();
      changed = true;
    }
  });

  // --- 9-11: Replace SyncHasMany<X>, AsyncHasMany<X>, AsyncBelongsTo<X> ---

  root.find(j.TSTypeReference).forEach((path) => {
    const typeName = path.node.typeName;
    if (j.Identifier.check(typeName)) {
      if (REMOVED_TYPE_SPECIFIERS.has(typeName.name)) {
        const typeParams = path.node.typeParameters;
        if (typeParams && typeParams.params.length > 0) {
          const innerType = typeParams.params[0];

          if (typeName.name === 'AsyncBelongsTo') {
            j(path).replaceWith(innerType);
          } else {
            j(path).replaceWith(j.tsArrayType(innerType));
          }
          changed = true;
        }
      }
    }
  });

  // Remove the type specifiers from all imports
  for (const typeName of REMOVED_TYPE_SPECIFIERS) {
    if (removeImport(j, root, typeName)) {
      changed = true;
    }
  }

  // --- 6: Add [Type] brand to Model classes ---

  root.find(j.ClassDeclaration).forEach((classPath) => {
    const superClass = classPath.node.superClass;
    if (!superClass) return;

    const isModelSubclass = isExtendingModel(j, root, superClass, fileInfo.path);
    if (!isModelSubclass) return;

    const modelName = deriveModelName(fileInfo.path);
    if (!modelName) return;

    const body = classPath.node.body.body;
    const hasTypeBrand = body.some((member) => {
      if (!j.ClassProperty.check(member)) return false;
      return (
        member.computed &&
        j.Identifier.check(member.key) &&
        member.key.name === 'Type'
      );
    });

    if (!hasTypeBrand) {
      const typeProp = j.classProperty(
        j.identifier('Type'),
        j.stringLiteral(modelName),
      ) as any;
      typeProp.computed = true;
      typeProp.declare = true;
      typeProp.typeAnnotation = j.tsTypeAnnotation(
        j.tsLiteralType(j.stringLiteral(modelName)),
      );
      typeProp.value = null;

      body.unshift(typeProp);

      addImport(j, root, 'Type', '@warp-drive/core/types/symbols');

      changed = true;
    }
  });

  // --- 7-8: Fix relationship decorators ---

  root.find(j.ClassDeclaration).forEach((classPath) => {
    const body = classPath.node.body.body;

    for (const member of body) {
      if (!j.ClassProperty.check(member)) continue;

      const decorators = getDecorators(member as any);
      for (const dec of decorators) {
        const name = getDecoratorName(dec);
        if (name !== 'hasMany' && name !== 'belongsTo') continue;

        const args = getDecoratorArgs(dec);
        if (args.length === 0) {
          // M7: Zero-arg @hasMany()/@belongsTo() is invalid — add a TODO comment
          const expr = dec.expression;
          if (expr?.type === 'CallExpression') {
            const propName =
              j.Identifier.check(member.key) ? member.key.name : 'unknown';
            const comment = j.commentLine(
              ` TODO: @${name}() requires a model type argument — e.g. @${name}('${propName}')`,
            );
            member.comments = member.comments ?? [];
            member.comments.unshift(comment);
            changed = true;
          }
          continue;
        }

        // Handle single-arg case: @hasMany('type') or @belongsTo('type')
        // Need to add { async: false, inverse: null } as second argument
        if (args.length === 1) {
          const expr = dec.expression;
          if (expr?.type === 'CallExpression') {
            const inverseProp = j.objectProperty(
              j.identifier('inverse'),
              j.nullLiteral(),
            ) as any;
            inverseProp.comments = [j.commentLine(' TODO: verify inverse value — null may be incorrect')];
            const optsObj = j.objectExpression([
              j.objectProperty(
                j.identifier('async'),
                j.booleanLiteral(false),
              ) as any,
              inverseProp,
            ]);
            expr.arguments.push(optsObj);
            changed = true;
          }
          continue;
        }

        const optsNode = args[1];
        if (optsNode?.type !== 'ObjectExpression') continue;

        const properties = optsNode.properties ?? [];

        // #8: Convert async: true → async: false (for BOTH hasMany AND belongsTo)
        for (const prop of properties) {
          if (
            prop.type === 'ObjectProperty' &&
            ((prop.key?.name === 'async') || (prop.key?.value === 'async')) &&
            prop.value?.type === 'BooleanLiteral' &&
            prop.value.value === true
          ) {
            prop.value.value = false;
            changed = true;
          }
        }

        // #8b: Add async: false if no async key exists at all
        const hasAsyncProp = properties.some(
          (p: any) =>
            p.type === 'ObjectProperty' &&
            (p.key?.name === 'async' || p.key?.value === 'async'),
        );
        if (!hasAsyncProp) {
          properties.push(
            j.objectProperty(j.identifier('async'), j.booleanLiteral(false)) as any,
          );
          changed = true;
        }

        // #7: Add inverse: null if missing
        const hasInverse = properties.some(
          (p: any) =>
            p.type === 'ObjectProperty' &&
            (p.key?.name === 'inverse' || p.key?.value === 'inverse'),
        );

        if (!hasInverse) {
          properties.push(
            j.objectProperty(j.identifier('inverse'), j.nullLiteral()) as any,
          );
          changed = true;
        }
      }
    }
  });

  if (!changed) return undefined;
  return root.toSource({ quote: 'single' });
}

/**
 * Derive model name from file path.
 * e.g. 'app/models/foo-bar.ts' → 'foo-bar'
 *      'app/models/task/signal.ts' → 'task/signal'
 */
function deriveModelName(filePath: string): string | null {
  const match = filePath.match(/models\/(.+?)\.(ts|js|gts|gjs)$/);
  if (!match) return null;
  return match[1];
}

/**
 * Check if a superClass identifier refers to a Model import.
 *
 * Fix #5: Also handles relative imports (e.g., `import BaseTask from '../task'`)
 * by resolving relative paths to check if they point to a model file.
 */
function isExtendingModel(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
  superClass: any,
  filePath: string = '',
): boolean {
  // L6: Handle mixin call patterns like `SortableMixin(Model)`
  // If the superclass is a CallExpression, check if any argument is Model
  if (j.CallExpression.check(superClass)) {
    for (const arg of superClass.arguments) {
      if (isExtendingModel(j, root, arg, filePath)) {
        return true;
      }
    }
    return false;
  }

  if (!j.Identifier.check(superClass)) return false;
  const name = superClass.name;

  // Known base class names that are Model subclasses
  if (name === 'Model') return true;

  let isModel = false;
  root.find(j.ImportDeclaration).forEach((importPath) => {
    const source = (importPath.node.source as any).value ?? '';

    const isModelPackage =
      source.includes('@ember-data/model') ||
      source.includes('@warp-drive/legacy/model') ||
      source.includes('/models/');

    // Fix #5/#M6: Handle relative imports that point to model files.
    // Relative imports starting with './' or '../' are almost always other models
    // when found in model files (since models live in app/models/).
    // Only treat relative imports as model imports when the file is in a models directory.
    // Exclude known non-model directories to avoid false positives.
    const isInModelsDir = filePath.includes('/models/');
    const isRelativeModelImport =
      isInModelsDir &&
      (source.startsWith('./') || source.startsWith('../')) &&
      !source.includes('/components/') &&
      !source.includes('/services/') &&
      !source.includes('/utils/') &&
      !source.includes('/mixins/') &&
      !source.includes('/helpers/') &&
      !source.includes('/config/') &&
      !source.includes('/initializers/') &&
      !source.includes('/instance-initializers/') &&
      !source.includes('/adapters/') &&
      !source.includes('/serializers/') &&
      !source.includes('/transforms/');

    if (isModelPackage || isRelativeModelImport) {
      const specs = importPath.node.specifiers ?? [];
      for (const spec of specs) {
        if (spec.local?.name === name) {
          isModel = true;
        }
      }
    }
  });

  return isModel;
}

/**
 * Walk up the AST from a path to find the nearest statement-level parent.
 */
function findParentStatement(path: any): any {
  let current = path;
  while (current?.parent) {
    const parentNode = current.parent.node;
    if (
      parentNode.type === 'ExpressionStatement' ||
      parentNode.type === 'VariableDeclaration' ||
      parentNode.type === 'ClassDeclaration' ||
      parentNode.type === 'ExportDefaultDeclaration' ||
      parentNode.type === 'ExportNamedDeclaration'
    ) {
      return current.parent;
    }
    current = current.parent;
  }
  return null;
}

export default withGtsSupport(transformer);
