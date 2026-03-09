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

export const TaskPhonemePairsSchema: LegacyResourceSchema = withDefaults({
  type: 'task/phoneme-pairs',
  fields: [
    ...BASE_TASK_FIELDS,
    { kind: 'attribute', name: 'contrastType', type: 'string' },
  ],
  objectExtensions: ['task-ext', 'task-phoneme-pairs-ext'],
}) as LegacyResourceSchema;

// Add tracked local fields
TaskPhonemePairsSchema.fields.push(...LOCAL_TASK_FIELDS);

export const TaskPhonemePairsExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'task-phoneme-pairs-ext',
  features: {
    get tasksToSolve() {
      const self = this as unknown as {
        answerOptions: IRawAnswerOption[];
      };
      const shuffled = shuffleArray(self.answerOptions, 2);
      const pairs: { answer: IRawAnswerOption[]; order: number }[] = [];
      for (let i = 0; i + 1 < shuffled.length; i += 2) {
        pairs.push({
          answer: [shuffled[i], shuffled[i + 1]],
          order: pairs.length,
        });
      }
      // If odd number of options, add the last one paired with the first
      if (shuffled.length % 2 !== 0 && shuffled.length > 0) {
        pairs.push({
          answer: [shuffled[shuffled.length - 1], shuffled[0]],
          order: pairs.length,
        });
      }
      return shuffleArray(pairs, 1);
    },
  },
};

export type TaskPhonemePairs = TaskBase & WithLegacy<{
  answerOptions: IRawAnswerOption[];
  contrastType: string;
  exerciseMechanism: ExerciseMechanism;
  tasksToSolve: { answer: IRawAnswerOption[]; order: number }[];
  [Type]: 'task/phoneme-pairs';
}>;
