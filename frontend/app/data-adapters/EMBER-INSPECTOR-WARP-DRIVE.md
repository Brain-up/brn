# Ember Inspector Data Tab with WarpDrive SchemaRecord (LegacyMode)

A guide for making the Ember Inspector's **Data** tab work with WarpDrive's `ReactiveResource` (SchemaRecord) in LegacyMode. This covers Ember 6.x, WarpDrive 5.x, Vite builds, and Ember Inspector v4.14.0+.

## The Problem

WarpDrive's `ReactiveResource` uses a **strict JavaScript Proxy**. In DEBUG mode, accessing any property not defined in the schema throws:

```
Error: No field named _debugInfo on subgroup
```

The Ember Inspector accesses several internal/undocumented properties on records when inspecting them. Classic `@ember-data/model` classes don't have this issue because they're regular ES classes with permissive property access. SchemaRecord's Proxy intercepts every `get` and asserts the field exists.

Additionally, `@ember-data/debug` (the package that historically provided the Data tab adapter) installs `_debugInfo` on Model prototypes — but SchemaRecord doesn't use Model prototypes, so the mechanism is completely bypassed.

## Prerequisites

- **Ember Inspector browser extension v4.14.0+** (Vite support added Oct 2025)
- **`@embroider/legacy-inspector-support`** installed and wired in `app/app.ts`:

```ts
// app/app.ts
import setupInspector from '@embroider/legacy-inspector-support/ember-source-4.12';

export default class App extends Application {
  // ... your config ...
  inspector = setupInspector(this);
}
```

This makes the Inspector detect your Vite-built app. Without it, the Ember tab won't appear in DevTools at all.

## Solution: Three Files

### 1. Register Inspector-Safe Fields on All Schemas

SchemaRecord's Proxy will throw on any unknown property access. The Inspector's `object-inspector.js` accesses several properties when you click on a record. You must register these as `@local` fields on every schema.

```ts
// In your schemas/index.ts (or wherever you collect ALL_SCHEMAS)

import type { LegacyModeFieldSchema } from '@warp-drive/core/types/schema/fields';

const INSPECTOR_FIELDS: LegacyModeFieldSchema[] = [
  { kind: '@local', name: '_debugInfo' },
  { kind: '@local', name: '_debugContainerKey' },
  { kind: '@local', name: 'content' },
  { kind: '@local', name: '_showProxyDetails' },
  { kind: '@local', name: 'get' },
  { kind: '@local', name: 'set' },
];

function withInspectorFields(schema: LegacyResourceSchema): LegacyResourceSchema {
  for (const field of INSPECTOR_FIELDS) {
    if (!schema.fields.some((f) => f.name === field.name)) {
      schema.fields.push(field);
    }
  }
  return schema;
}

export const ALL_SCHEMAS = [
  MySchema1,
  MySchema2,
  // ...
].map(withInspectorFields);
```

### 2. Create a Custom DataAdapter

The Data tab requires a `data-adapter:main` registration. Since WarpDrive SchemaRecord doesn't use `model:*` factory registrations, the base class's `detect()` + `containerDebugAdapter` discovery flow won't find your types. Override `watchModelTypes` to enumerate types from your schema registry directly.

```ts
// app/data-adapters/main.ts

import DataAdapter from '@ember/debug/data-adapter';
import { service } from '@ember/service';
import { A } from '@ember/array';
import type { NativeArray } from '@ember/array';
import type Store from 'your-app/services/store';
import { ALL_SCHEMAS } from 'your-app/schemas';

type RecordColor = 'black' | 'red' | 'blue' | 'green';
type Column = { name: string; desc: string };

interface LegacyRecord {
  id: string | null;
  isNew: boolean;
  isDeleted: boolean;
  hasDirtyAttributes: boolean;
  [key: string]: unknown;
}

function humanize(str: string): string {
  return str
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/[_-]/g, ' ')
    .replace(/^\w/, (c) => c.toUpperCase());
}

export default class WarpDriveDataAdapter extends DataAdapter<LegacyRecord> {
  @service('store') declare store: Store;

  get _schemaTypes(): string[] {
    return ALL_SCHEMAS.map((s) => s.type);
  }

  getFilters(): Array<{ name: string; desc: string }> {
    return [
      { name: 'isNew', desc: 'New' },
      { name: 'isModified', desc: 'Modified' },
      { name: 'isClean', desc: 'Clean' },
    ];
  }

  detect(): boolean {
    return false; // Not used — we override watchModelTypes
  }

  watchModelTypes(
    typesAdded: (types: Array<{ name: string; count: number; columns: Column[]; object: unknown }>) => void,
    typesUpdated: (types: Array<{ name: string; count: number; columns: Column[]; object: unknown }>) => void,
  ): () => void {
    const types = this._schemaTypes.map((typeName) => {
      const records = this.store.peekAll(typeName);
      return {
        name: typeName,
        count: records.length,
        columns: this._columnsForTypeName(typeName),
        object: typeName,
      };
    });

    typesAdded(types);

    // Poll for count changes
    const interval = setInterval(() => {
      const updated = this._schemaTypes.map((typeName) => {
        const records = this.store.peekAll(typeName);
        return {
          name: typeName,
          count: records.length,
          columns: this._columnsForTypeName(typeName),
          object: typeName,
        };
      });
      typesUpdated(updated);
    }, 3000);

    return () => clearInterval(interval);
  }

  _columnsForTypeName(typeName: string): Column[] {
    const columns: Column[] = [{ name: 'id', desc: 'Id' }];
    const fields = this.store.schema.fields({ type: typeName });
    const limit = this.attributeLimit || 100;
    let count = 0;

    fields.forEach((field, name) => {
      if (count >= limit) return;
      if (field.kind === 'attribute') {
        columns.push({ name, desc: humanize(name) });
        count++;
      }
    });

    return columns;
  }

  columnsForType(klass: unknown): Column[] {
    if (typeof klass === 'string') {
      return this._columnsForTypeName(klass);
    }
    return [{ name: 'id', desc: 'Id' }];
  }

  getRecords(_klass: unknown, name: string): NativeArray<LegacyRecord> {
    return A(Array.from(this.store.peekAll(name))) as NativeArray<LegacyRecord>;
  }

  getRecordColumnValues(record: LegacyRecord): Record<string, unknown> {
    const values: Record<string, unknown> = { id: record.id };
    const typeName = (record.constructor as { modelName?: string })?.modelName;
    if (!typeName) return values;

    const fields = this.store.schema.fields({ type: typeName });
    const limit = this.attributeLimit || 100;
    let count = 0;

    fields.forEach((field, name) => {
      if (count >= limit) return;
      if (field.kind === 'attribute') {
        try {
          values[name] = record[name];
        } catch {
          values[name] = undefined;
        }
        count++;
      }
    });

    return values;
  }

  getRecordKeywords(record: LegacyRecord): NativeArray<unknown> {
    const keywords: unknown[] = [record.id];
    const typeName = (record.constructor as { modelName?: string })?.modelName;
    if (!typeName) return A(keywords);

    const fields = this.store.schema.fields({ type: typeName });
    fields.forEach((field, name) => {
      if (field.kind === 'attribute') {
        try {
          const val = record[name];
          if (typeof val === 'string' || typeof val === 'number') {
            keywords.push(val);
          }
        } catch {
          // skip inaccessible fields
        }
      }
    });

    return A(keywords);
  }

  getRecordFilterValues(record: LegacyRecord): Record<string, boolean> {
    return {
      isNew: record.isNew,
      isModified: record.hasDirtyAttributes && !record.isNew,
      isClean: !record.hasDirtyAttributes,
    };
  }

  getRecordColor(record: LegacyRecord): RecordColor {
    if (record.isNew) return 'green';
    if (record.hasDirtyAttributes) return 'blue';
    return 'black';
  }

  _nameToClass(type: string): unknown {
    return type;
  }
}
```

### 3. Register the Adapter via Instance Initializer

```ts
// app/instance-initializers/data-adapter.ts

import type ApplicationInstance from '@ember/application/instance';
import WarpDriveDataAdapter from 'your-app/data-adapters/main';

export function initialize(appInstance: ApplicationInstance): void {
  appInstance.register('data-adapter:main', WarpDriveDataAdapter);
}

export default {
  name: 'warp-drive-data-adapter',
  initialize,
};
```

## Why Each Inspector Field Is Needed

The Ember Inspector's `object-inspector.js` and `data-debug.js` access these properties on record objects. Without them, SchemaRecord's strict Proxy throws in DEBUG mode.

### Fields you MUST add as `@local`

| Field | Inspector Source | What It Does |
|---|---|---|
| `_debugInfo` | `getDebugInfo()` L1264 | Reads `record._debugInfo` for property group display (Attributes, Relationships, Flags) |
| `_debugContainerKey` | `isInternalProperty()` | Used as a fallback for model name detection |
| `content` | `mixinsForObject()` L695 | Checks `object.content` after ObjectProxy instanceof (short-circuits if not ObjectProxy, but Proxy `get` trap fires first) |
| `_showProxyDetails` | `mixinsForObject()` L696 | Controls whether to inspect proxy internals vs content |
| `get` | `calculateCP()` L1320 | Calls `object.get?.(property)` to resolve computed properties |
| `set` | `saveProperty()` L466 | Checks `object.set` for property editing support |

### Fields you DON'T need (already handled)

**By `withDefaults()` legacy derivations** (registered as `kind: 'derived', type: '@legacy'`):

`isNew`, `hasDirtyAttributes`, `isDeleted`, `isEmpty`, `isError`, `isLoaded`, `isLoading`, `isSaving`, `isValid`, `constructor`, `currentState`, `dirtyType`, `errors`, `adapterError`, `belongsTo`, `hasMany`, `changedAttributes`, `deleteRecord`, `destroyRecord`, `reload`, `rollbackAttributes`, `save`, `serialize`, `unloadRecord`, `_createSnapshot`

**By `withDefaults()` as `@local` fields:**

`isDestroying`, `isDestroyed`, `_isReloading`

**By the Proxy's built-in handling:**

`toString`, `toJSON`, `toHTML`, `constructor` (also in legacy derivations), `length`, `nodeType`, `then`, `setInterval`, `document`, all Symbols

## How the Proxy Works (for reference)

The `get` trap in `@warp-drive/core/reactive/-private/record.ts` follows this priority:

1. Internal WarpDrive symbols (`Context`, `Destroy`, `Checkout`, `Commit`, `Signals`) -> handled
2. `'___notifications'` -> internal state
3. Schema field found in `fields.get(prop)` -> return field value
4. `IgnoredGlobalFields` (`length`, `nodeType`, `then`, `setInterval`, `document`) -> return `undefined`
5. Built-in methods (`toString`, `toJSON`, `toHTML`, `constructor`) -> bound functions
6. `Symbol.toPrimitive`, `Symbol.iterator`, `Symbol.toStringTag` -> handled
7. Extension properties -> delegated to extension
8. Any other symbol -> return `undefined`
9. **Any other string -> `throw new Error('No field named ...')`** (DEBUG only; returns `undefined` in prod)

This means in production builds the errors are silent (returns `undefined`), but in development you'll get assertion errors for every unknown property access. The `@local` field registration makes the Proxy recognize these properties and return `undefined` through the normal field resolution path instead of hitting the assertion.

## Key Differences from `@ember-data/debug`

| Aspect | `@ember-data/debug` | This adapter |
|---|---|---|
| Type discovery | `containerDebugAdapter` + `detect()` | Direct schema registry enumeration |
| `_debugInfo` | Installed on Model prototype | Registered as `@local` field (returns `undefined`) |
| Record access | `eachAttribute()` on Model class | `store.schema.fields()` from SchemaService |
| Model name | `Model.modelName` static property | `record.constructor.modelName` (from legacy `constructor` derivation) |
| Update tracking | `store.notifications.subscribe()` | `setInterval` polling (3s) |

## Version Compatibility

| Package | Version | Notes |
|---|---|---|
| `ember-source` | 6.x | Tested with 6.8.3 |
| `@warp-drive/core` | 5.x | Tested with 5.8.1 |
| `@warp-drive/legacy` | 5.x | Required for `withDefaults()` LegacyMode |
| `@embroider/vite` | 1.x | Tested with 1.6.0 |
| `@embroider/legacy-inspector-support` | 0.1.x | Required for Vite app detection |
| Ember Inspector extension | 4.14.0+ | Required for Vite support |

## Known Limitations

- **No property grouping**: Since `_debugInfo` returns `undefined` (not a function), the Inspector's object panel shows all properties flat instead of grouped into "Attributes", "Relationships", "Flags". Providing a proper `_debugInfo` function would require a custom extension.
- **Polling for updates**: Record count updates use `setInterval(3s)` instead of `store.notifications.subscribe()`. The notification API could be used for more responsive updates but adds complexity.
- **No relationship columns**: Only `attribute` fields appear as columns. `belongsTo` and `hasMany` relationships are accessible via record inspection but not shown in the table view.

## References

- [Ember Inspector source: `object-inspector.js`](https://github.com/emberjs/ember-inspector/blob/main/ember_debug/object-inspector.js)
- [Ember Inspector source: `data-debug.js`](https://github.com/emberjs/ember-inspector/blob/main/ember_debug/data-debug.js)
- [WarpDrive SchemaRecord Proxy](https://github.com/warp-drive-data/warp-drive/blob/main/warp-drive-packages/core/src/reactive/-private/record.ts)
- [WarpDrive `withDefaults()` (LegacyMode)](https://github.com/warp-drive-data/warp-drive/blob/main/packages/legacy/src/model/migration-support.ts)
- [WarpDrive `@ember-data/debug` data-adapter](https://github.com/warp-drive-data/warp-drive/blob/main/packages/debug/src/data-adapter.ts)
- [WarpDrive Issue #8818: Improved Debugging Experience](https://github.com/warp-drive-data/warp-drive/issues/8818)
- [`@embroider/legacy-inspector-support`](https://github.com/embroider-build/embroider/tree/main/packages/legacy-inspector-support)
- [Base `DataAdapter` class in ember-source](https://github.com/emberjs/ember.js/blob/main/packages/%40ember/debug/data-adapter.ts)
