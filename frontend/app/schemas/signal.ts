import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';

export const SignalSchema: LegacyResourceSchema = withDefaults({
  type: 'signal',
  fields: [
    { kind: 'attribute', name: 'frequency', type: 'number' },
    { kind: 'attribute', name: 'duration', type: 'number' },
    { kind: 'attribute', name: 'length', type: 'number' },
    { kind: 'attribute', name: 'name', type: 'string' },
    { kind: 'attribute', name: 'url', type: 'string' },
  ],
});

export type Signal = WithLegacy<{
  frequency: number;
  duration: number;
  length: number;
  name: string;
  url: string;
  [Type]: 'signal';
}>;
