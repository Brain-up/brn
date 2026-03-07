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

import type { LegacyResourceSchema, LegacyModeFieldSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
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

/**
 * Fields that the Ember Inspector accesses on records via its object inspector.
 * SchemaRecord uses a strict Proxy that throws on unknown field access,
 * so we register these as @local fields returning undefined.
 *
 * Already handled by withDefaults() legacy derivations:
 *   isNew, hasDirtyAttributes, isDeleted, isDestroying, isDestroyed,
 *   constructor, currentState, errors, etc.
 *
 * Already handled by the Proxy itself:
 *   toString, toJSON, toHTML, constructor, length, nodeType, then, symbols
 */
const INSPECTOR_FIELDS: LegacyModeFieldSchema[] = [
  // object-inspector.js getDebugInfo() — reads record._debugInfo for property grouping
  { kind: '@local', name: '_debugInfo' },
  // object-inspector.js isInternalProperty() — container key lookup
  { kind: '@local', name: '_debugContainerKey' },
  // object-inspector.js mixinsForObject() — ObjectProxy content unwrapping check
  { kind: '@local', name: 'content' },
  // object-inspector.js mixinsForObject() — proxy detail display toggle
  { kind: '@local', name: '_showProxyDetails' },
  // object-inspector.js calculateCP() — optional chained record.get?.()
  { kind: '@local', name: 'get' },
  // object-inspector.js saveProperty() — record.set for property editing
  { kind: '@local', name: 'set' },
];

function withInspectorFields(schema: LegacyResourceSchema): LegacyResourceSchema {
  for (const field of INSPECTOR_FIELDS) {
    if (!schema.fields.some((f) => f.name === field.name)) {
      schema.fields.push(field);
    }
  }
  return schema;
}

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
].map(withInspectorFields);

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
