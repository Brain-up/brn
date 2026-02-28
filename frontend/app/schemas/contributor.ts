import { withDefaults, type WithLegacy } from '@warp-drive-mirror/legacy/model/migration-support';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive-mirror/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive-mirror/core/reactive';

type ContributorKind = 'DEVELOPER' | 'SPECIALIST' | 'QA' | 'DESIGNER' | 'OTHER';

export const ContributorSchema: LegacyResourceSchema = withDefaults({
  type: 'contributor',
  fields: [
    { kind: 'attribute', name: 'rawName' },
    { kind: 'attribute', name: 'rawDescription' },
    { kind: 'attribute', name: 'rawCompany' },
    { kind: 'attribute', name: 'avatar', type: 'string' },
    { kind: 'attribute', name: 'contribution', type: 'number' },
    { kind: 'attribute', name: 'isActive', type: 'boolean' },
    { kind: 'attribute', name: 'login', type: 'string' },
    { kind: 'attribute', name: 'kind', type: 'string' },
    { kind: 'attribute', name: 'contacts' },
  ],
  objectExtensions: ['contributor-ext'],
});

/**
 * Extension that adds locale-dependent computed getters (name, description, company)
 * to Contributor schema records.
 *
 * The old Model injected the user-data service for activeLocale. Since extensions
 * don't support DI, we approximate using navigator.language. The intl service
 * should be used at the component/template level for full locale correctness.
 */
export const ContributorExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'contributor-ext',
  features: {
    get locale(): string {
      return (typeof navigator !== 'undefined' && navigator.language) || 'en';
    },
    get name(): string {
      const self = this as unknown as { rawName: Record<string, string>; locale: string };
      return self.rawName?.[self.locale] ?? '';
    },
    get description(): string {
      const self = this as unknown as { rawDescription: Record<string, string>; locale: string };
      return self.rawDescription?.[self.locale] ?? '';
    },
    get company(): string {
      const self = this as unknown as { rawCompany: Record<string, string>; locale: string };
      return self.rawCompany?.[self.locale] ?? '';
    },
  },
};

export type Contributor = WithLegacy<{
  rawName: Record<string, string>;
  rawDescription: Record<string, string>;
  rawCompany: Record<string, string>;
  avatar: string;
  contribution: number;
  isActive: boolean;
  login: string;
  kind: ContributorKind;
  contacts: { type: string; value: string }[];
  locale: string;
  name: string;
  description: string;
  company: string;
  [Type]: 'contributor';
}>;
