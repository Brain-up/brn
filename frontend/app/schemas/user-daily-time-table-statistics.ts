import { withDefaults, type WithLegacy } from '@warp-drive-mirror/legacy/model/migration-support';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive-mirror/core/types/schema/fields';

export const UserDailyTimeTableStatisticsSchema: LegacyResourceSchema = withDefaults({
  type: 'user-daily-time-table-statistics',
  fields: [
    { kind: 'attribute', name: 'seriesName', type: 'string' },
    { kind: 'attribute', name: 'allDoneExercises', type: 'number' },
    { kind: 'attribute', name: 'uniqueDoneExercises', type: 'number' },
    { kind: 'attribute', name: 'repeatedExercises', type: 'number' },
    { kind: 'attribute', name: 'doneExercisesSuccessfullyFromFirstTime', type: 'number' },
    { kind: 'attribute', name: 'listenWordsCount', type: 'number' },
  ],
});

export type UserDailyTimeTableStatistics = WithLegacy<{
  seriesName: string;
  allDoneExercises: number;
  uniqueDoneExercises: number;
  repeatedExercises: number;
  doneExercisesSuccessfullyFromFirstTime: number;
  listenWordsCount: number;
  [Type]: 'user-daily-time-table-statistics';
}>;
