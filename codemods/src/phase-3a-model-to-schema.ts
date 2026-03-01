import type { API, FileInfo, Options, ClassDeclaration } from 'jscodeshift';
import { classifyMember, getDecorators, getDecoratorName } from './utils/decorators';
import {
  buildSchemaFile,
  buildModelStub,
  toPascalCase,
  classifyMembersForSchema,
} from './utils/schema-builder';
import type { SchemaInfo } from './utils/schema-builder';
import { withGtsSupport } from './utils/gts-support';
import * as path from 'path';
import * as fs from 'fs';

/**
 * Phase 3a: Model to Schema Transform
 *
 * Extract field definitions from ember-data Model classes into WarpDrive
 * schema scaffolds. Generate extension shells with TODO markers.
 * Overwrite model files with re-export stubs.
 *
 * Config (via --options or jscodeshift options):
 *  - appName: Application name (default: 'app')
 *  - schemasDir: Output directory for schemas relative to app/ (default: 'app/schemas')
 *  - modelsDir: Models directory relative to app/ (default: 'app/models')
 *  - baseOnlyClasses: Classes that are base-only (never instantiated) (default: ['CompletionDependent'])
 *  - customTransforms: Map of custom transform names to TS types
 */

interface Phase3aOptions {
  appName?: string;
  schemasDir?: string;
  modelsDir?: string;
  baseOnlyClasses?: string[];
  customTransforms?: Record<string, { tsType: string; importFrom?: string }>;
  dryRun?: boolean;
}

const DEFAULT_CUSTOM_TRANSFORMS: Record<string, { tsType: string; importFrom?: string }> = {
  'full-date': { tsType: 'DateTime', importFrom: 'luxon' },
  array: { tsType: 'unknown[]' },
  date: { tsType: 'Date' },
};

function transformer(
  fileInfo: FileInfo,
  api: API,
  options: Options,
): string | undefined {
  const j = api.jscodeshift;
  const root = j(fileInfo.source);

  const opts: Phase3aOptions = options as any;
  const appName = opts.appName ?? 'app';
  const baseOnlyClasses = opts.baseOnlyClasses ?? [
    'CompletionDependent',
    'CompletionDependentModel',
  ];
  const customTransforms = {
    ...DEFAULT_CUSTOM_TRANSFORMS,
    ...(opts.customTransforms ?? {}),
  };
  const dryRun = opts.dryRun ?? false;

  // 1. Find the default-exported class declaration
  const classDecl = findDefaultExportedClass(j, root);
  if (!classDecl) return undefined;

  // 2. Skip base-only classes
  let className = classDecl.id?.name ?? '';
  if (!className) {
    // Anonymous default export class — derive name from file path
    const baseName = path.basename(fileInfo.path, path.extname(fileInfo.path));
    className = toPascalCase(baseName);
  }
  if (baseOnlyClasses.includes(className)) {
    return undefined; // Skip, keep as legacy Model
  }

  // 3. Extract model name from file path
  const modelName = deriveModelName(fileInfo.path);
  if (!modelName) return undefined;

  // 4. Detect parent class
  const parentClassName = getParentClassName(j, classDecl);

  // 5. Collect named exports
  const namedExports = collectNamedExports(j, root, fileInfo.source);

  // 6. Classify all class body members
  const members = classDecl.body.body.map((member) =>
    classifyMember(j, member, fileInfo.source),
  );

  // 7. Build schema info
  const { fields, localFields, services, features } = classifyMembersForSchema(
    members,
    customTransforms,
  );

  const pascalName = toPascalCase(modelName);

  const schemaInfo: SchemaInfo = {
    modelName,
    className: pascalName,
    schemaVarName: `${pascalName}Schema`,
    extensionVarName: `${pascalName}Extension`,
    selfTypeName: `${pascalName}Self`,
    typeAliasName: pascalName,
    fields,
    localFields,
    services,
    features,
    namedExports,
    parentClass: parentClassName,
    appName,
    customTransforms,
  };

  // 8. Generate schema file content
  const schemaContent = buildSchemaFile(schemaInfo);

  // 9. Generate model stub content
  const stubContent = buildModelStub(schemaInfo);

  // 10. Write schema file
  if (!dryRun) {
    const schemasDir = opts.schemasDir ?? resolveSchemaDir(fileInfo.path);
    const schemaFilePath = path.join(schemasDir, `${modelName}.ts`);

    // Ensure directory exists
    const schemaFileDir = path.dirname(schemaFilePath);
    if (!fs.existsSync(schemaFileDir)) {
      fs.mkdirSync(schemaFileDir, { recursive: true });
    }

    fs.writeFileSync(schemaFilePath, schemaContent, 'utf-8');

    // Log what we did
    api.report?.(`  Created schema: ${schemaFilePath}`);
    if (services.length > 0) {
      api.report?.(
        `  WARNING: ${modelName} has ${services.length} service(s) requiring manual wiring: ${services.map((s) => s.serviceName).join(', ')}`,
      );
    }
    if (features.length > 0) {
      api.report?.(
        `  WARNING: ${modelName} has ${features.length} extension feature(s) requiring manual this→self rewriting`,
      );
    }
    for (const field of fields) {
      if (
        (field.kind === 'hasMany' || field.kind === 'belongsTo') &&
        field.options?.inverse === null
      ) {
        api.report?.(
          `  WARNING: ${modelName}.${field.name} has inverse: null (placeholder — verify correct value)`,
        );
      }
    }
  }

  // 11. Return the model stub (overwrites the model file)
  return stubContent;
}

/**
 * Find the default-exported class declaration.
 */
function findDefaultExportedClass(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
): ClassDeclaration | null {
  // Pattern 1: export default class Foo extends Model { ... }
  const exportDefault = root.find(j.ExportDefaultDeclaration);
  if (exportDefault.length > 0) {
    const decl = exportDefault.get().node.declaration;
    if (j.ClassDeclaration.check(decl)) {
      return decl;
    }
    // Pattern 2: class Foo extends Model { ... }; export default Foo;
    if (j.Identifier.check(decl)) {
      const name = decl.name;
      const classDeclPath = root.find(j.ClassDeclaration, {
        id: { name },
      });
      if (classDeclPath.length > 0) {
        return classDeclPath.get().node;
      }
    }
  }

  return null;
}

/**
 * Get the name of the parent class.
 */
function getParentClassName(
  j: API['jscodeshift'],
  classDecl: ClassDeclaration,
): string {
  const superClass = classDecl.superClass;
  if (!superClass) return '';
  if (j.Identifier.check(superClass)) return superClass.name;
  return '';
}

/**
 * Derive model name from file path.
 */
function deriveModelName(filePath: string): string | null {
  const match = filePath.match(/models\/(.+?)\.(ts|js|gts|gjs)$/);
  if (!match) return null;
  return match[1];
}

/**
 * Resolve the schemas directory from a model file path.
 * e.g. /path/to/app/models/foo.ts → /path/to/app/schemas/
 */
function resolveSchemaDir(modelFilePath: string): string {
  const modelsIdx = modelFilePath.lastIndexOf('/models/');
  if (modelsIdx === -1) {
    // Fallback
    return path.join(path.dirname(modelFilePath), '..', 'schemas');
  }
  const appDir = modelFilePath.substring(0, modelsIdx);
  return path.join(appDir, 'schemas');
}

/**
 * Collect named exports (interfaces, types, enums) for preservation.
 */
function collectNamedExports(
  j: API['jscodeshift'],
  root: ReturnType<API['jscodeshift']>,
  sourceCode: string,
): Array<{ name: string; kind: string; sourceText: string }> {
  const exports: Array<{ name: string; kind: string; sourceText: string }> = [];
  const seenNames = new Set<string>();

  // Collect exported interfaces/types/enums
  root.find(j.ExportNamedDeclaration).forEach((path) => {
    const decl = path.node.declaration;
    if (!decl) return;

    if (j.TSInterfaceDeclaration.check(decl)) {
      const name = (decl.id as any).name;
      seenNames.add(name);
      exports.push({
        name,
        kind: 'interface',
        sourceText: extractNodeSource(decl, sourceCode, 'export '),
      });
    } else if (j.TSTypeAliasDeclaration.check(decl)) {
      const name = (decl.id as any).name;
      seenNames.add(name);
      exports.push({
        name,
        kind: 'type',
        sourceText: extractNodeSource(decl, sourceCode, 'export '),
      });
    } else if (j.TSEnumDeclaration.check(decl)) {
      const name = (decl.id as any).name;
      seenNames.add(name);
      exports.push({
        name,
        kind: 'enum',
        sourceText: extractNodeSource(decl, sourceCode, 'export '),
      });
    } else if (j.VariableDeclaration.check(decl)) {
      // M13: Collect exported const/let/var declarations
      for (const declarator of decl.declarations) {
        if (j.Identifier.check((declarator as any).id)) {
          const name = (declarator as any).id.name;
          seenNames.add(name);
          exports.push({
            name,
            kind: 'VariableDeclaration',
            sourceText: extractNodeSource(decl, sourceCode, 'export '),
          });
        }
      }
    } else if (j.FunctionDeclaration.check(decl)) {
      // M13: Collect exported function declarations
      const name = decl.id?.name;
      if (name) {
        seenNames.add(name);
        exports.push({
          name,
          kind: 'FunctionDeclaration',
          sourceText: extractNodeSource(decl, sourceCode, 'export '),
        });
      }
    } else if (j.ClassDeclaration.check(decl)) {
      // M13: Collect exported class declarations
      const name = decl.id?.name;
      if (name) {
        seenNames.add(name);
        exports.push({
          name,
          kind: 'ClassDeclaration',
          sourceText: extractNodeSource(decl, sourceCode, 'export '),
        });
      }
    }
  });

  return exports;
}

function extractNodeSource(
  node: any,
  sourceCode: string,
  prefix: string = '',
): string {
  if (node.start != null && node.end != null) {
    return prefix + sourceCode.slice(node.start, node.end);
  }
  return '';
}

export default withGtsSupport(transformer);
