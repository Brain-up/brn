import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import { BASE_TASK_FIELDS, LOCAL_TASK_FIELDS, type TaskBase } from '../task';
import shuffleArray from 'brn/utils/shuffle-array';
import { ExerciseMechanism } from 'brn/utils/exercise-types';

interface IRawAnswerOption {
  audioFileUrl: string;
  description: string;
  id: number;
  columnNumber: number;
  pictureFileUrl: string;
  soundsCount: number;
  word: string;
  wordType: 'OBJECT';
}

export const TaskProsodySchema: LegacyResourceSchema = withDefaults({
  type: 'task/prosody',
  fields: [
    ...BASE_TASK_FIELDS,
    { kind: 'attribute', name: 'prosodyType', type: 'string' },
  ],
  objectExtensions: ['task-ext', 'task-prosody-ext'],
}) as LegacyResourceSchema;

// Add tracked local fields
TaskProsodySchema.fields.push(...LOCAL_TASK_FIELDS);

export const TaskProsodyExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'task-prosody-ext',
  features: {
    get tasksToSolve() {
      const self = this as unknown as {
        answerOptions: IRawAnswerOption[];
      };
      const options = self.answerOptions;
      const tasks: { answer: IRawAnswerOption[]; order: number }[] = [];

      // Generate single-selection tasks from prosody contrast sets.
      // Each task presents 2-3 options where the user picks the matching prosody.
      const shuffled = shuffleArray(options, 2);
      const groupSize = Math.min(3, shuffled.length);

      for (let i = 0; i + groupSize - 1 < shuffled.length; i += groupSize) {
        const group = shuffled.slice(i, i + groupSize);
        tasks.push({
          answer: group,
          order: tasks.length,
        });
      }

      // Handle remaining options that don't fill a complete group
      const remainder = shuffled.length % groupSize;
      if (remainder > 0 && remainder < shuffled.length) {
        const remaining = shuffled.slice(shuffled.length - remainder);
        // Pad with items from the beginning to reach at least 2 options
        while (remaining.length < 2 && shuffled.length >= 2) {
          remaining.push(shuffled[remaining.length % shuffled.length]);
        }
        tasks.push({
          answer: remaining,
          order: tasks.length,
        });
      }

      return shuffleArray(tasks, 1);
    },
  },
};

export type TaskProsody = TaskBase & WithLegacy<{
  answerOptions: IRawAnswerOption[];
  prosodyType: string;
  exerciseMechanism: ExerciseMechanism;
  tasksToSolve: { answer: IRawAnswerOption[]; order: number }[];
  [Type]: 'task/prosody';
}>;
