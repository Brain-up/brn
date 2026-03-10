import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';

export const AudiometrySchema: LegacyResourceSchema = withDefaults({
  type: 'audiometry',
  fields: [
    { kind: 'attribute', name: 'locale', type: 'string' },
    { kind: 'attribute', name: 'name', type: 'string' },
    { kind: 'attribute', name: 'audiometryType', type: 'string' },
    { kind: 'attribute', name: 'description', type: 'string' },
    { kind: 'attribute', name: 'audiometryTasks' },
  ],
});

export type Audiometry = WithLegacy<{
  locale: string;
  name: string;
  audiometryType: string;
  description: string;
  audiometryTasks: AudiometryTask[];
  [Type]: 'audiometry';
}>;

export interface AudiometryTask {
  id: string;
  frequencyZone?: number;
  audiometryGroup?: string;
}
