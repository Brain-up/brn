import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';

export const SignalSchema: LegacyResourceSchema = withDefaults({
  type: 'signal',
  fields: [
    { kind: 'attribute', name: 'frequency', type: 'number' },
    { kind: 'attribute', name: 'duration', type: 'number' },
  ],
});

export type Signal = WithLegacy<{
  frequency: number;
  duration: number;
  [Type]: 'signal';
}>;
