import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import { sortByKey } from 'brn/utils/sort-by-key';

export const SeriesSchema: LegacyResourceSchema = withDefaults({
  type: 'series',
  fields: [
    { kind: 'attribute', name: 'name', type: 'string' },
    { kind: 'attribute', name: 'description', type: 'string' },
    { kind: 'attribute', name: 'level', type: 'number' },
    { kind: 'attribute', name: 'kind', type: 'string' },
    { kind: 'attribute', name: 'type', type: 'string' },
    { kind: 'attribute', name: 'active', type: 'boolean' },
    { kind: 'attribute', name: 'exerciseGroupId', type: 'number' },
    { kind: 'attribute', name: 'order', type: 'number' },
    {
      kind: 'belongsTo',
      name: 'group',
      type: 'group',
      options: { async: false, inverse: 'series' },
    },
    {
      kind: 'hasMany',
      name: 'subGroups',
      type: 'subgroup',
      options: { async: false, inverse: null },
    },
    {
      kind: 'hasMany',
      name: 'exercises',
      type: 'exercise',
      options: { async: false, inverse: 'series' },
    },
  ],
  objectExtensions: ['series-ext'],
});

interface ExerciseLike {
  order: number;
  name: string;
  isCompleted?: boolean;
}

export const SeriesExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'series-ext',
  features: {
    get children() {
      const self = this as unknown as { exercises: ExerciseLike[] };
      return self.exercises || [];
    },
    get parent() {
      const self = this as unknown as { group: unknown };
      return self.group;
    },
    set parent(value: unknown) {
      const self = this as unknown as { group: unknown };
      self.group = value;
    },
    get sortedExercises() {
      const self = this as unknown as { exercises: ExerciseLike[] };
      return sortByKey(Array.from(self.exercises || []), 'order');
    },
    get sortedChildren() {
      const self = this as unknown as { sortedExercises: ExerciseLike[] };
      return self.sortedExercises;
    },
    get groupedByNameExercises(): Record<string, ExerciseLike[]> {
      const self = this as unknown as { exercises: ExerciseLike[] };
      // Group exercises by name, then sort each group by order once
      const groups: Record<string, ExerciseLike[]> = {};
      for (const exercise of Array.from(self.exercises || [])) {
        const { name } = exercise;
        if (!groups[name]) {
          groups[name] = [];
        }
        groups[name].push(exercise);
      }
      for (const name of Object.keys(groups)) {
        groups[name] = sortByKey(groups[name], 'order');
      }
      return groups;
    },
  },
};

export type Series = WithLegacy<{
  name: string;
  description: string;
  level: number;
  kind: string;
  type: string;
  active: boolean;
  exerciseGroupId: number;
  order: number;
  group: unknown;
  subGroups: unknown[];
  exercises: ExerciseLike[];
  children: ExerciseLike[];
  parent: unknown;
  sortedExercises: ExerciseLike[];
  sortedChildren: ExerciseLike[];
  groupedByNameExercises: Record<string, ExerciseLike[]>;
  [Type]: 'series';
}>;
