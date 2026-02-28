import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema, LegacyModeFieldSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import arrayPreviousItems from 'brn/utils/array-previous-items';
import { getService } from 'brn/utils/schema-helpers';
import { sortByKey } from 'brn/utils/sort-by-key';
import type TasksManagerService from 'brn/services/tasks-manager';

export const GroupSchema: LegacyResourceSchema = withDefaults({
  type: 'group',
  fields: [
    { kind: 'attribute', name: 'name', type: 'string' },
    { kind: 'attribute', name: 'description', type: 'string' },
    { kind: 'attribute', name: 'locale', type: 'string' },
    { kind: 'attribute', name: 'order', type: 'number' },
    {
      kind: 'hasMany',
      name: 'series',
      type: 'series',
      options: { async: false, inverse: 'group' },
    },
  ],
  objectExtensions: ['group-ext'],
}) as LegacyResourceSchema;

// Add @local fields (tracked mutable state, not persisted)
GroupSchema.fields.push(
  {
    kind: '@local',
    name: 'isManuallyCompleted',
    type: 'boolean',
    options: { defaultValue: false },
  } as LegacyModeFieldSchema,
  {
    kind: '@local',
    name: 'available',
    type: 'boolean',
    options: { defaultValue: false },
  } as LegacyModeFieldSchema,
);

interface SeriesLike {
  id?: string | null;
  isCompleted?: boolean;
  [key: string]: unknown;
}

export const GroupExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'group-ext',
  features: {
    /**
     * Ember's <LinkTo> component accesses `isQueryParams` to check if a model
     * is actually a QueryParams object. SchemaRecord's strict proxy throws
     * on unknown fields, so we expose this to prevent that error.
     */
    get isQueryParams() {
      return undefined;
    },

    get parent() {
      return null;
    },

    get sortChildrenBy() {
      return 'id';
    },

    get children(): SeriesLike[] {
      const self = this as unknown as { series: SeriesLike[] };
      return Array.from(self.series || []);
    },

    get sortedChildren(): SeriesLike[] | null {
      const self = this as unknown as {
        children: SeriesLike[];
        sortChildrenBy: string;
      };
      const children = self.children;
      if (!children) return null;
      return sortByKey(Array.from(children).filter(Boolean), self.sortChildrenBy);
    },

    get sortedSeries(): SeriesLike[] {
      const self = this as unknown as { sortedChildren: SeriesLike[] | null };
      return (self.sortedChildren as SeriesLike[]) || [];
    },

    get isCompleted(): boolean {
      const self = this as unknown as {
        isManuallyCompleted: boolean;
        children: SeriesLike[];
      };
      if (self.isManuallyCompleted) {
        return true;
      }
      const tasksManager = getService<TasksManagerService>(self, 'tasks-manager');
      if (!tasksManager || tasksManager.completedTasks.length === 0) {
        return false;
      }
      const children = self.children;
      const validChildren = children ? Array.from(children).filter(Boolean) : [];
      return (
        validChildren.length > 0 &&
        validChildren.every((child) => child.isCompleted)
      );
    },

    // Group is top-level, no parent → empty siblings
    get allSiblings(): unknown[] {
      return [];
    },

    get previousSiblings(): unknown[] {
      const self = this as unknown as { allSiblings: unknown[] };
      return arrayPreviousItems(self, self.allSiblings);
    },

    get nextSiblings(): unknown[] {
      const self = this as unknown as { allSiblings: unknown[] };
      return self.allSiblings.slice(self.allSiblings.indexOf(self) + 1);
    },

    get isFirst(): boolean {
      const self = this as unknown as { previousSiblings: unknown[] };
      return self.previousSiblings.length === 0;
    },

    get canInteract(): boolean {
      const self = this as unknown as {
        available?: boolean;
        previousSiblings: unknown[];
      };
      if (self.available) {
        return true;
      }
      return (
        self.previousSiblings.length === 0 ||
        self.previousSiblings.every((sibling: unknown) => (sibling as { isCompleted: boolean }).isCompleted)
      );
    },
  },
};

export type Group = WithLegacy<{
  name: string;
  description: string;
  locale: string;
  order: number;
  series: SeriesLike[];
  parent: null;
  sortChildrenBy: string;
  children: SeriesLike[];
  sortedChildren: SeriesLike[] | null;
  sortedSeries: SeriesLike[];
  isManuallyCompleted: boolean;
  isCompleted: boolean;
  allSiblings: unknown[];
  previousSiblings: unknown[];
  nextSiblings: unknown[];
  isFirst: boolean;
  canInteract: boolean;
  [Type]: 'group';
}>;
