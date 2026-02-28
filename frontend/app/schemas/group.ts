import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import { storeFor } from '@warp-drive/core';
import { getOwner } from '@ember/application';
import arrayPreviousItems from 'brn/utils/array-previous-items';
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

// Add isManuallyCompleted as a @local field (tracked mutable state, not persisted)
GroupSchema.fields.push({
  kind: '@local',
  name: 'isManuallyCompleted',
  type: 'boolean',
  options: { defaultValue: false },
} as any);

/**
 * Helper to look up the tasks-manager service from a record instance.
 */
function getTasksManager(record: unknown): TasksManagerService | null {
  const store = storeFor(record as any, true);
  if (!store) return null;
  const owner = getOwner(store);
  if (!owner) return null;
  return owner.lookup('service:tasks-manager') as TasksManagerService;
}

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

    /**
     * Group is a top-level entity, so parent is always null.
     */
    get parent() {
      return null;
    },

    /**
     * Override from CompletionDependent: sort children by 'id' instead of 'order'.
     */
    get sortChildrenBy() {
      return 'id';
    },

    /**
     * Children are the series records.
     */
    get children(): SeriesLike[] {
      const self = this as unknown as { series: SeriesLike[] };
      return Array.from(self.series || []);
    },

    /**
     * Sort children (series) by the sortChildrenBy key ('id').
     */
    get sortedChildren(): SeriesLike[] | null {
      const self = this as unknown as {
        children: SeriesLike[];
        sortChildrenBy: string;
      };
      const children = self.children;
      if (!children) return null;
      const key = self.sortChildrenBy;
      return Array.from(children)
        .filter(Boolean)
        .sort((a: any, b: any) => {
          const aVal = a[key];
          const bVal = b[key];
          if (aVal < bVal) return -1;
          if (aVal > bVal) return 1;
          return 0;
        });
    },

    /**
     * Convenience getter for sorted series.
     */
    get sortedSeries(): SeriesLike[] {
      const self = this as unknown as { sortedChildren: SeriesLike[] | null };
      return (self.sortedChildren as SeriesLike[]) || [];
    },

    /**
     * Whether this group is completed (all children are completed,
     * or manually marked as completed).
     * isManuallyCompleted is defined as a @local field on the schema.
     */
    get isCompleted(): boolean {
      const self = this as unknown as {
        isManuallyCompleted: boolean;
        children: SeriesLike[];
      };
      if (self.isManuallyCompleted) {
        return true;
      }
      const tasksManager = getTasksManager(self);
      if (!tasksManager || tasksManager.completedTasks.length === 0) {
        return false;
      }
      const children = self.children;
      const validChildren = children ? Array.from(children).filter(Boolean) : [];
      return (
        validChildren.length > 0 &&
        validChildren.every((child: any) => child.isCompleted)
      );
    },

    /**
     * All siblings (from parent's sortedChildren). Group has no parent, so empty.
     */
    get allSiblings(): unknown[] {
      return [];
    },

    /**
     * Siblings that come before this entity.
     */
    get previousSiblings(): unknown[] {
      const self = this as unknown as { allSiblings: unknown[] };
      return arrayPreviousItems(self, self.allSiblings);
    },

    /**
     * Siblings that come after this entity.
     */
    get nextSiblings(): unknown[] {
      const self = this as unknown as { allSiblings: unknown[] };
      return self.allSiblings.slice(self.allSiblings.indexOf(self) + 1);
    },

    /**
     * Whether this is the first among its siblings.
     */
    get isFirst(): boolean {
      const self = this as unknown as { previousSiblings: unknown[] };
      return self.previousSiblings.length === 0;
    },

    /**
     * Whether this entity can be interacted with.
     */
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
        self.previousSiblings.every((sibling: any) => sibling.isCompleted)
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
