import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import { getService } from 'brn/utils/schema-helpers';
import type UserDataService from 'brn/services/user-data';

type ContributorKind = 'DEVELOPER' | 'SPECIALIST' | 'QA' | 'AUTOTESTER' | 'DESIGNER' | 'OTHER';

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
    { kind: 'attribute', name: 'repositoryName', type: 'string' },
  ],
  objectExtensions: ['contributor-ext'],
});

/**
 * Extension that adds locale-dependent computed getters (name, description, company)
 * to Contributor schema records.
 *
 * Uses storeFor/getOwner to look up the user-data service for activeLocale,
 * matching the original Model's behavior of respecting the app's locale switcher.
 */
export const ContributorExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'contributor-ext',
  features: {
    get locale(): string {
      const userData = getService<UserDataService>(this, 'user-data');
      return userData?.activeLocale || 'en-us';
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
  repositoryName: string;
  locale: string;
  name: string;
  description: string;
  company: string;
  [Type]: 'contributor';
}>;
