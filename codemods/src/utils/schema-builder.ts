import type { MemberClassification } from './decorators';

export interface SchemaField {
  kind: 'attribute' | 'belongsTo' | 'hasMany';
  name: string;
  type?: string | null;
  options?: Record<string, any>;
  /** Original TS type annotation from the model, for Self interface */
  tsType?: string | null;
  /** Raw options source text from @attr second argument */
  rawOptions?: string | null;
}

export interface LocalField {
  kind: '@local';
  name: string;
  defaultValue: string | null;
  tsType: string | null;
}

export interface ServiceRecord {
  name: string;
  serviceName: string;
}

export interface ExtensionFeature {
  featureKind: 'getter' | 'setter' | 'method' | 'getter-closure' | 'readonly-value';
  name: string;
  body: string;
  isCached?: boolean;
}

export interface SchemaInfo {
  modelName: string;
  className: string;
  schemaVarName: string;
  extensionVarName: string;
  selfTypeName: string;
  typeAliasName: string;
  fields: SchemaField[];
  localFields: LocalField[];
  services: ServiceRecord[];
  features: ExtensionFeature[];
  namedExports: Array<{ name: string; kind: string; sourceText: string }>;
  parentClass: string;
  appName: string;
  customTransforms: Record<string, { tsType: string; importFrom?: string }>;
}

/**
 * Convert a kebab-case model name to PascalCase.
 * e.g. 'foo-bar' → 'FooBar', 'task/signal' → 'TaskSignal'
 */
export function toPascalCase(name: string): string {
  return name
    .split(/[-/_]/)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join('');
}

/**
 * Convert a kebab-case model name to camelCase.
 * e.g. 'foo-bar' → 'fooBar', 'task/signal' → 'taskSignal'
 */
export function toCamelCase(name: string): string {
  const pascal = toPascalCase(name);
  return pascal.charAt(0).toLowerCase() + pascal.slice(1);
}

/**
 * Build the complete schema file content.
 */
export function buildSchemaFile(info: SchemaInfo): string {
  const lines: string[] = [];
  const imports = collectImports(info);

  // Imports
  for (const imp of imports) {
    lines.push(imp);
  }

  lines.push('');

  // Service wiring note (if services detected)
  if (info.services.length > 0) {
    lines.push(`// TODO: Wire services manually. Services detected in original model:`);
    for (const svc of info.services) {
      lines.push(`//   @service('${svc.serviceName}') ${svc.name}`);
    }
    lines.push(`// Use store.getService() or pass services via extension context.`);
    lines.push('');
  }

  // Self interface
  lines.push(buildSelfInterface(info));
  lines.push('');

  // Schema definition (includes @local fields inline)
  lines.push(buildSchemaConst(info));
  lines.push('');

  // Extension (if features exist)
  if (info.features.length > 0 || info.services.length > 0) {
    lines.push(buildExtension(info));
    lines.push('');
  }

  // Type alias
  lines.push(buildTypeAlias(info));
  lines.push('');

  // Named exports preservation
  for (const exp of info.namedExports) {
    lines.push(exp.sourceText);
    lines.push('');
  }

  // Registration reminder comments
  lines.push(`// TODO: Register this schema in your app's store setup:`);
  lines.push(`//   store.schema.registerResource(${info.schemaVarName});`);
  lines.push(`//   registerDerivations(store.schema);`);
  if (info.features.length > 0 || info.services.length > 0) {
    lines.push(`//   store.schema.CAUTION_MEGA_DANGER_ZONE_registerExtension(${info.extensionVarName});`);
  }
  lines.push('');

  return lines.join('\n');
}

/**
 * Build the model stub file content.
 */
export function buildModelStub(info: SchemaInfo): string {
  const lines: string[] = [];

  lines.push(
    `export type { ${info.typeAliasName} as default } from '${info.appName}/schemas/${info.modelName}';`,
  );

  // Re-export named exports
  // Runtime values (enums, consts, functions, classes) use `export { Name }`.
  // Type-only exports (interfaces, type aliases) use `export type { Name }`.
  const VALUE_EXPORT_KINDS = new Set([
    'TSEnumDeclaration',
    'enum',
    'VariableDeclaration',
    'FunctionDeclaration',
    'ClassDeclaration',
  ]);

  for (const exp of info.namedExports) {
    if (VALUE_EXPORT_KINDS.has(exp.kind)) {
      lines.push(
        `export { ${exp.name} } from '${info.appName}/schemas/${info.modelName}';`,
      );
    } else {
      lines.push(
        `export type { ${exp.name} } from '${info.appName}/schemas/${info.modelName}';`,
      );
    }
  }

  lines.push('');

  return lines.join('\n');
}

function collectImports(info: SchemaInfo): string[] {
  const imports: string[] = [];

  // Core schema construction imports
  imports.push(
    `import { withDefaults } from '@warp-drive/legacy/model/migration-support';`,
  );
  imports.push(
    `import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';`,
  );

  // Type symbol import — value import (not `import type`) because [Type] is used as
  // a computed property key in the Self interface, which requires the runtime symbol value.
  imports.push(
    `import { Type } from '@warp-drive/core/types/symbols';`,
  );

  if (info.features.length > 0 || info.services.length > 0) {
    imports.push(
      `import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';`,
    );
  }

  // Collect custom transform imports
  const transformImports = new Map<string, Set<string>>();
  for (const field of info.fields) {
    if (field.type && info.customTransforms[field.type]) {
      const ct = info.customTransforms[field.type];
      if (ct.importFrom) {
        if (!transformImports.has(ct.importFrom)) {
          transformImports.set(ct.importFrom, new Set());
        }
        transformImports.get(ct.importFrom)!.add(ct.tsType);
      }
    }
  }
  for (const [source, types] of transformImports) {
    imports.push(`import type { ${[...types].join(', ')} } from '${source}';`);
  }

  // WithLegacy type
  imports.push(
    `import type { WithLegacy } from '@warp-drive/legacy/model/migration-support';`,
  );

  // Collect relationship type names that need to be imported
  const relTypeNames = new Set<string>();
  for (const field of info.fields) {
    if ((field.kind === 'belongsTo' || field.kind === 'hasMany') && field.type) {
      relTypeNames.add(toPascalCase(field.type));
    }
  }
  if (relTypeNames.size > 0) {
    imports.push('');
    imports.push('// TODO: Import related model types:');
    for (const typeName of relTypeNames) {
      const kebab = info.fields.find(
        (f) => (f.kind === 'belongsTo' || f.kind === 'hasMany') && f.type && toPascalCase(f.type) === typeName,
      )?.type;
      imports.push(`//   import type { ${typeName} } from '${info.appName}/schemas/${kebab}';`);
    }
  }

  return imports;
}

function buildSelfInterface(info: SchemaInfo): string {
  const lines: string[] = [];
  lines.push(`interface ${info.selfTypeName} {`);

  // [Type] brand — required for WithLegacy<T> which constrains T extends TypedRecordInstance
  lines.push(`  [Type]: '${info.modelName}';`);

  for (const field of info.fields) {
    const tsType = resolveFieldType(field, info.customTransforms);
    lines.push(`  ${field.name}: ${tsType};`);
  }

  for (const local of info.localFields) {
    const tsType = local.tsType ?? 'unknown';
    lines.push(`  ${local.name}: ${tsType};`);
  }

  lines.push(`}`);
  return lines.join('\n');
}

function resolveFieldType(
  field: SchemaField,
  customTransforms: Record<string, { tsType: string; importFrom?: string }>,
): string {
  if (field.kind === 'belongsTo') {
    return `${toPascalCase(field.type ?? 'unknown')} | null`;
  }
  if (field.kind === 'hasMany') {
    return `${toPascalCase(field.type ?? 'unknown')}[]`;
  }
  // Fix #3: If original TS type annotation is available, prefer it
  if (field.tsType) {
    return field.tsType;
  }
  if (field.type && customTransforms[field.type]) {
    return customTransforms[field.type].tsType;
  }
  switch (field.type) {
    case 'string':
      return 'string';
    case 'number':
      return 'number';
    case 'boolean':
      return 'boolean';
    case 'date':
      return 'Date';
    default:
      return 'unknown';
  }
}

function buildSchemaConst(info: SchemaInfo): string {
  const lines: string[] = [];
  const hasExtensions = info.features.length > 0 || info.services.length > 0;
  lines.push(
    `export const ${info.schemaVarName} = withDefaults({`,
  );
  lines.push(`  type: '${info.modelName}',`);
  lines.push(`  fields: [`);

  for (const field of info.fields) {
    lines.push(`    ${buildFieldEntry(field)},`);
  }

  // @local fields inline in the fields array
  for (const local of info.localFields) {
    lines.push(`    ${buildLocalFieldEntry(local)},`);
  }

  lines.push(`  ],`);

  if (hasExtensions) {
    lines.push(`  objectExtensions: ['${info.modelName}-ext'],`);
  }

  lines.push(`}) as LegacyResourceSchema;`);

  return lines.join('\n');
}

/**
 * Serialize a plain object into JS object literal syntax (unquoted keys).
 * e.g. { async: false, inverse: null } instead of {"async":false,"inverse":null}
 */
function toJSObject(obj: Record<string, any>): string {
  const entries = Object.entries(obj).map(([k, v]) => {
    const val = typeof v === 'string' ? `'${v}'` : JSON.stringify(v);
    return `${k}: ${val}`;
  });
  return `{ ${entries.join(', ')} }`;
}

function buildFieldEntry(field: SchemaField): string {
  const parts: string[] = [`kind: '${field.kind}'`, `name: '${field.name}'`];

  if (field.type != null) {
    parts.push(`type: '${field.type}'`);
  }

  if (field.options && Object.keys(field.options).length > 0) {
    parts.push(`options: ${toJSObject(field.options)}`);
  }

  const entry = `{ ${parts.join(', ')} }`;

  // M10: If @attr had raw options (e.g. { defaultValue: '' }), emit a TODO comment
  // so developers know the option existed and wasn't silently dropped.
  if (field.rawOptions) {
    return `${entry} /* TODO: defaultValue was ${field.rawOptions} — handle at handler/transform layer */`;
  }

  return entry;
}

function buildLocalFieldEntry(local: LocalField): string {
  const parts: string[] = [`kind: '@local'`, `name: '${local.name}'`];
  if (local.defaultValue != null) {
    parts.push(`options: { defaultValue: ${local.defaultValue} }`);
  }
  return `{ ${parts.join(', ')} }`;
}

function buildExtension(info: SchemaInfo): string {
  const lines: string[] = [];

  lines.push(`export const ${info.extensionVarName}: CAUTION_MEGA_DANGER_ZONE_Extension = {`);
  lines.push(`  kind: 'object',`);
  lines.push(`  name: '${info.modelName}-ext',`);

  if (info.services.length > 0) {
    lines.push(`  // Services needed — wire manually:`);
    for (const svc of info.services) {
      lines.push(`  //   ${svc.name} → TODO: pass service '${svc.serviceName}' via store.getService()`);
    }
  }

  lines.push(`  features: {`);

  for (const feature of info.features) {
    lines.push(buildFeatureEntry(feature, info));
  }

  lines.push(`  },`);
  lines.push(`};`);

  return lines.join('\n');
}

function buildFeatureEntry(feature: ExtensionFeature, info: SchemaInfo): string {
  const lines: string[] = [];

  switch (feature.featureKind) {
    case 'getter':
      lines.push(`    get ${feature.name}() {`);
      lines.push(`      const self = this as unknown as ${info.selfTypeName};`);
      // M12: If the original getter had @cached, emit a note so developers know
      if (feature.isCached) {
        lines.push(`      // NOTE: Was @cached in original model`);
      }
      lines.push(`      // TODO: Rewrite this.propName → self.propName`);
      lines.push(`      // TODO: Replace @service injections with getService(self, 'service-name')`);
      lines.push(`      // Original body:`);
      lines.push(`      ${formatBody(feature.body)}`);
      lines.push(`    },`);
      break;

    case 'setter':
      lines.push(`    set ${feature.name}(value: any) {`);
      lines.push(`      const self = this as unknown as ${info.selfTypeName};`);
      lines.push(`      // TODO: Rewrite this.propName → self.propName`);
      lines.push(`      // Original body:`);
      lines.push(`      ${formatBody(feature.body)}`);
      lines.push(`    },`);
      break;

    case 'method':
      lines.push(`    ${feature.name}(...args: any[]) {`);
      lines.push(`      const self = this as unknown as ${info.selfTypeName};`);
      lines.push(`      // TODO: Rewrite this.propName → self.propName`);
      lines.push(`      // TODO: Replace @service injections with getService(self, 'service-name')`);
      lines.push(`      // Original body:`);
      lines.push(`      ${formatBody(feature.body)}`);
      lines.push(`    },`);
      break;

    case 'getter-closure':
      lines.push(`    get ${feature.name}() {`);
      lines.push(`      const self = this as unknown as ${info.selfTypeName};`);
      lines.push(`      // TODO: This was an @action — rewrite as getter returning closure`);
      lines.push(`      // TODO: Rewrite this.propName → self.propName`);
      lines.push(`      // Original body:`);
      lines.push(`      return () => {`);
      lines.push(`        ${formatBody(feature.body)}`);
      lines.push(`      };`);
      lines.push(`    },`);
      break;

    case 'readonly-value':
      lines.push(`    get ${feature.name}() {`);
      lines.push(`      return ${feature.body};`);
      lines.push(`    },`);
      break;
  }

  return lines.join('\n');
}

function formatBody(body: string): string {
  // Remove outer braces if present
  let trimmed = body.trim();
  if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
    trimmed = trimmed.slice(1, -1).trim();
  }
  return trimmed;
}

function buildTypeAlias(info: SchemaInfo): string {
  return `export type ${info.typeAliasName} = WithLegacy<${info.selfTypeName}>;`;
}

/**
 * Classify members from the AST into schema building blocks.
 */
export function classifyMembersForSchema(
  members: MemberClassification[],
  customTransforms: Record<string, { tsType: string; importFrom?: string }>,
): {
  fields: SchemaField[];
  localFields: LocalField[];
  services: ServiceRecord[];
  features: ExtensionFeature[];
} {
  const fields: SchemaField[] = [];
  const localFields: LocalField[] = [];
  const services: ServiceRecord[] = [];
  const features: ExtensionFeature[] = [];

  for (const member of members) {
    switch (member.kind) {
      case 'attribute': {
        const isEmptyOrNull = member.attrType == null || member.attrType === '';
        // In LegacyMode (withDefaults), ALL @attr decorators use kind: 'attribute'.
        // Untyped @attr() omits the type property; typed @attr('string') includes it.
        fields.push({
          kind: 'attribute',
          name: member.name,
          type: isEmptyOrNull ? undefined : member.attrType,
          tsType: member.tsType,
          rawOptions: member.options,
        });
        break;
      }

      case 'belongsTo':
        fields.push({
          kind: 'belongsTo',
          name: member.name,
          type: member.relatedType,
          options: normalizeRelOptions(member.options),
        });
        break;

      case 'hasMany':
        fields.push({
          kind: 'hasMany',
          name: member.name,
          type: member.relatedType,
          options: normalizeRelOptions(member.options),
        });
        break;

      case 'tracked':
        localFields.push({
          kind: '@local',
          name: member.name,
          defaultValue: member.defaultValue,
          tsType: member.tsType,
        });
        break;

      case 'service':
        services.push({
          name: member.name,
          serviceName: member.serviceName,
        });
        break;

      case 'getter':
        features.push({
          featureKind: 'getter',
          name: member.name,
          body: member.body,
          isCached: member.isCached,
        });
        break;

      case 'setter':
        features.push({
          featureKind: 'setter',
          name: member.name,
          body: member.body,
        });
        break;

      case 'method':
        features.push({
          featureKind: member.isAction ? 'getter-closure' : 'method',
          name: member.name,
          body: member.body,
        });
        break;

      case 'property':
        features.push({
          featureKind: 'readonly-value',
          name: member.name,
          body: member.body,
        });
        break;

      // 'declare' and 'unknown' are skipped
    }
  }

  return { fields, localFields, services, features };
}

function normalizeRelOptions(options: any): Record<string, any> {
  if (!options || typeof options !== 'object') return { async: false, inverse: null };

  const result: Record<string, any> = {};

  // Extract properties from AST ObjectExpression
  if (options.type === 'ObjectExpression' && Array.isArray(options.properties)) {
    for (const prop of options.properties) {
      // M11: Skip spread elements — emit a TODO marker instead
      if (prop.type === 'SpreadElement' || prop.type === 'SpreadProperty') {
        const spreadSrc = prop.argument?.name ?? '/* unknown spread */';
        result[`/* TODO: ...${spreadSrc} */`] = '/* spread */';
        continue;
      }

      // M11: Handle computed keys — fall back to a TODO marker
      const key = prop.computed
        ? `/* computed: [${prop.key?.name ?? prop.key?.value ?? '?'}] */`
        : (prop.key?.name ?? prop.key?.value);

      if (!key) continue;

      const value = prop.value;
      if (!value) continue;

      if (value.type === 'BooleanLiteral') {
        result[key] = value.value;
      } else if (value.type === 'StringLiteral') {
        result[key] = value.value;
      } else if (value.type === 'NullLiteral') {
        result[key] = null;
      } else if (value.type === 'NumericLiteral') {
        result[key] = value.value;
      } else if (value.type === 'Identifier') {
        // M11: Identifier — use identifier name as fallback
        result[key] = `/* TODO: was identifier '${value.name}' */`;
      } else if (value.type === 'ArrayExpression') {
        // M11: ArrayExpression — extract literal values where possible
        const elements = (value.elements ?? []).map((el: any) => {
          if (!el) return 'null';
          if (el.type === 'StringLiteral') return `'${el.value}'`;
          if (el.type === 'NumericLiteral') return String(el.value);
          if (el.type === 'BooleanLiteral') return String(el.value);
          if (el.type === 'NullLiteral') return 'null';
          if (el.type === 'Identifier') return el.name;
          return '/* complex */';
        });
        result[key] = `/* TODO: was [${elements.join(', ')}] */`;
      } else {
        // M11: Other complex values — emit TODO with type info
        result[key] = `/* TODO: was ${value.type} */`;
      }
    }
  } else {
    // Already a plain object
    Object.assign(result, options);
  }

  // Ensure async: false
  result.async = false;

  // Ensure inverse exists (placeholder)
  if (!('inverse' in result)) {
    result.inverse = null;
  }

  return result;
}
