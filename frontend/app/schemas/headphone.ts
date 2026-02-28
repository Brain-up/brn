import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';

export const HeadphoneSchema: LegacyResourceSchema = withDefaults({
  type: 'headphone',
  fields: [
    { kind: 'attribute', name: 'description', type: 'string' },
    { kind: 'attribute', name: 'name', type: 'string' },
  ],
});

export type Headphone = WithLegacy<{
  description: string;
  name: string;
  [Type]: 'headphone';
}>;
