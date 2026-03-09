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

export const TaskAuditorySequenceSchema: LegacyResourceSchema = withDefaults({
  type: 'task/auditory-sequence',
  fields: [
    ...BASE_TASK_FIELDS,
  ],
  objectExtensions: ['task-ext', 'task-auditory-sequence-ext'],
}) as LegacyResourceSchema;

// Add tracked local fields
TaskAuditorySequenceSchema.fields.push(...LOCAL_TASK_FIELDS);

export const TaskAuditorySequenceExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'task-auditory-sequence-ext',
  features: {
    get tasksToSolve() {
      const self = this as unknown as {
        answerOptions: IRawAnswerOption[];
        exercise: { playWordsCount?: number };
      };
      const sequenceLength = self.exercise.playWordsCount ?? 3;
      const options = self.answerOptions;
      const tasks: { answer: IRawAnswerOption[]; order: number }[] = [];

      // Generate multiple sequence tasks by shuffling and slicing
      for (let round = 1; round <= 3; round++) {
        const shuffled = shuffleArray(options, round);
        const sequence = shuffled.slice(0, Math.min(sequenceLength, shuffled.length));
        tasks.push({
          answer: sequence,
          order: tasks.length,
        });
      }

      return tasks;
    },
  },
};

export type TaskAuditorySequence = TaskBase & WithLegacy<{
  answerOptions: IRawAnswerOption[];
  exerciseMechanism: ExerciseMechanism;
  tasksToSolve: { answer: IRawAnswerOption[]; order: number }[];
  [Type]: 'task/auditory-sequence';
}>;
