import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import type { DateTime } from 'luxon';
import { secondsTo } from 'brn/utils/seconds-to';
import type { UserExercisingProgressStatusType } from './user-weekly-statistics-types';

export { PROGRESS, type UserExercisingProgressStatusType } from './user-weekly-statistics-types';

export const UserWeeklyStatisticsSchema: LegacyResourceSchema = withDefaults({
  type: 'user-weekly-statistics',
  fields: [
    // Use kind: 'field' with type: 'full-date' so that the registered
    // FullDateTransformation.hydrate() is called on get, converting
    // the raw ISO string from the cache into a Luxon DateTime.
    { kind: 'field', name: 'date', type: 'full-date' },
    { kind: 'attribute', name: 'exercisingTimeSeconds', type: 'number' },
    { kind: 'attribute', name: 'progress' },
  ],
  objectExtensions: ['user-weekly-statistics-ext'],
});

/**
 * Extension that adds computed getters (time, month, year) to
 * UserWeeklyStatistics schema records.
 *
 * These were previously defined on the Model class.
 * Using navigator.language for locale since schema records
 * don't support DI (the Model used userData.activeLocale).
 */
export const UserWeeklyStatisticsExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'user-weekly-statistics-ext',
  features: {
    get time(): string {
      const self = this as unknown as { exercisingTimeSeconds: number };
      return secondsTo(self.exercisingTimeSeconds, 'h:m:s');
    },
    get month(): string {
      const self = this as unknown as { date: DateTime };
      const locale = (typeof navigator !== 'undefined' && navigator.language) || 'en';
      return self.date.toFormat('MMMM', { locale });
    },
    get year(): number {
      const self = this as unknown as { date: DateTime };
      return self.date.year;
    },
  },
};

export type UserWeeklyStatistics = WithLegacy<{
  date: DateTime;
  exercisingTimeSeconds: number;
  progress: UserExercisingProgressStatusType;
  time: string;
  month: string;
  year: number;
  [Type]: 'user-weekly-statistics';
}>;
