import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';
import { BASE_TASK_FIELDS, LOCAL_TASK_FIELDS, type TaskBase } from '../task';

export const TaskSignalSchema: LegacyResourceSchema = withDefaults({
  type: 'task/signal',
  fields: [
    ...BASE_TASK_FIELDS,
    {
      kind: 'belongsTo',
      name: 'signal',
      type: 'signal',
      options: { async: false, inverse: null },
    },
  ],
  objectExtensions: ['task-ext'],
}) as LegacyResourceSchema;

// Add tracked local fields matching the base task
TaskSignalSchema.fields.push(...LOCAL_TASK_FIELDS);

export type TaskSignal = TaskBase & WithLegacy<{
  signal: unknown;
  [Type]: 'task/signal';
}>;
