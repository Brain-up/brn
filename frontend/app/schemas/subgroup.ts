import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import { storeFor } from '@warp-drive/core';

/**
 * Minimal interface for Exercise properties accessed through Subgroup.
 * Avoids circular dependency with the Exercise model/schema.
 */
interface ExerciseRef {
  id?: string | null;
  available: boolean;
  isCompleted?: boolean;
  isManuallyCompleted?: boolean;
  [key: string]: unknown;
}

export const SubgroupSchema: LegacyResourceSchema = withDefaults({
  type: 'subgroup',
  fields: [
    { kind: 'attribute', name: 'seriesId', type: 'string' },
    { kind: 'attribute', name: 'name', type: 'string' },
    { kind: 'attribute', name: 'level', type: 'number' },
    { kind: 'attribute', name: 'pictureUrl', type: 'string' },
    { kind: 'attribute', name: 'description', type: 'string' },
    { kind: 'attribute', name: 'order', type: 'number' },
    {
      kind: 'hasMany',
      name: 'exercises',
      type: 'exercise',
      options: { async: false, inverse: 'parent' },
    },
  ],
  objectExtensions: ['subgroup-ext'],
});

export const SubgroupExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'subgroup-ext',
  features: {
    get picture(): string {
      const self = this as unknown as { pictureUrl: string };
      return self.pictureUrl;
    },
    get parent(): unknown {
      const self = this as unknown as { seriesId: string };
      const store = storeFor(self as any, true);
      if (!store) return null;
      return store.peekRecord('series', self.seriesId);
    },
    get count(): number {
      const self = this as unknown as { exercisesIds: string[] };
      return self.exercisesIds.length;
    },
    get exercisesIds(): string[] {
      const self = this as unknown as { exercises: ExerciseRef[] };
      return (self.exercises || []).map((e: ExerciseRef) => e.id).filter(Boolean) as string[];
    },
  },
};

export type Subgroup = WithLegacy<{
  seriesId: string;
  name: string;
  level: number;
  pictureUrl: string;
  description: string;
  order: number;
  exercises: ExerciseRef[];
  picture: string;
  parent: unknown;
  count: number;
  exercisesIds: string[];
  [Type]: 'subgroup';
}>;
