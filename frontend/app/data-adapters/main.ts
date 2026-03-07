import DataAdapter from '@ember/debug/data-adapter';
import { service } from '@ember/service';
import { A } from '@ember/array';
import type { NativeArray } from '@ember/array';
import type Store from 'brn/services/store';
import { ALL_SCHEMAS } from 'brn/schemas';

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
    // Not used — we override watchModelTypes
    return false;
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

    // Poll for count changes (store notifications don't cover all cases)
    const interval = setInterval(() => {
      const updated = this._schemaTypes
        .map((typeName) => {
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
