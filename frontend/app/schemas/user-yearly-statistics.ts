import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import type { DateTime } from 'luxon';
import { secondsTo } from 'brn/utils/seconds-to';
import type { UserExercisingProgressStatusType } from './user-weekly-statistics-types';

export const UserYearlyStatisticsSchema: LegacyResourceSchema = withDefaults({
  type: 'user-yearly-statistics',
  fields: [
    { kind: 'field', name: 'date', type: 'full-date' },
    { kind: 'attribute', name: 'exercisingTimeSeconds', type: 'number' },
    { kind: 'attribute', name: 'progress' },
    { kind: 'attribute', name: 'exercisingDays', type: 'number' },
  ],
  objectExtensions: ['user-yearly-statistics-ext'],
});

/**
 * Extension that adds computed getters (time, month, year, days) to
 * UserYearlyStatistics schema records.
 *
 * Includes the same getters as the weekly schema plus `days`.
 */
export const UserYearlyStatisticsExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'user-yearly-statistics-ext',
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
    get days(): number {
      const self = this as unknown as { exercisingDays: number };
      return self.exercisingDays;
    },
  },
};

export type UserYearlyStatistics = WithLegacy<{
  date: DateTime;
  exercisingTimeSeconds: number;
  progress: UserExercisingProgressStatusType;
  exercisingDays: number;
  time: string;
  month: string;
  year: number;
  days: number;
  [Type]: 'user-yearly-statistics';
}>;
