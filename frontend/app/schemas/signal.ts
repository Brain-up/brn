import { withDefaults, type WithLegacy } from '@warp-drive-mirror/legacy/model/migration-support';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive-mirror/core/types/schema/fields';

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
