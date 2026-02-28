/**
 * Central registry for all SchemaRecord schemas.
 *
 * Each schema replaces a class-based Model with a JSON schema definition
 * that the store uses with ReactiveResource (SchemaRecord in LegacyMode).
 *
 * Schemas are registered via the `useLegacyStore({ schemas })` option.
 * Types with registered schemas get ReactiveResource instances;
 * types without schemas continue using Model classes via the DelegatingSchemaService.
 */

import type { LegacyResourceSchema } from '@warp-drive-mirror/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive-mirror/core/reactive';
import { SignalSchema } from './signal';
import { HeadphoneSchema } from './headphone';
import { UserDailyTimeTableStatisticsSchema } from './user-daily-time-table-statistics';
import { UserWeeklyStatisticsSchema, UserWeeklyStatisticsExtension } from './user-weekly-statistics';
import { UserYearlyStatisticsSchema, UserYearlyStatisticsExtension } from './user-yearly-statistics';
import { ContributorSchema, ContributorExtension } from './contributor';
import { SeriesSchema, SeriesExtension } from './series';
import { SubgroupSchema, SubgroupExtension } from './subgroup';
import { GroupSchema, GroupExtension } from './group';
import { ExerciseSchema, ExerciseExtension } from './exercise';
import { TaskSchema, TaskExtension } from './task';
import { TaskSignalSchema } from './task/signal';
import { TaskSingleSimpleWordsSchema, TaskSingleSimpleWordsExtension } from './task/single-simple-words';
import { TaskWordsSequencesSchema, TaskWordsSequencesExtension } from './task/words-sequences';

export const ALL_SCHEMAS: LegacyResourceSchema[] = [
  SignalSchema,
  HeadphoneSchema,
  UserDailyTimeTableStatisticsSchema,
  UserWeeklyStatisticsSchema,
  UserYearlyStatisticsSchema,
  ContributorSchema,
  SeriesSchema,
  SubgroupSchema,
  GroupSchema,
  ExerciseSchema,
  TaskSchema,
  TaskSignalSchema,
  TaskSingleSimpleWordsSchema,
  TaskWordsSequencesSchema,
];

export const ALL_EXTENSIONS: CAUTION_MEGA_DANGER_ZONE_Extension[] = [
  UserWeeklyStatisticsExtension,
  UserYearlyStatisticsExtension,
  ContributorExtension,
  SeriesExtension,
  SubgroupExtension,
  GroupExtension,
  ExerciseExtension,
  TaskExtension,
  TaskSingleSimpleWordsExtension,
  TaskWordsSequencesExtension,
];
