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

export const TaskEnvironmentalSoundsSchema: LegacyResourceSchema = withDefaults({
  type: 'task/environmental-sounds',
  fields: [
    ...BASE_TASK_FIELDS,
  ],
  objectExtensions: ['task-ext', 'task-environmental-sounds-ext'],
}) as LegacyResourceSchema;

// Add tracked local fields
TaskEnvironmentalSoundsSchema.fields.push(...LOCAL_TASK_FIELDS);

export const TaskEnvironmentalSoundsExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'task-environmental-sounds-ext',
  features: {
    get tasksToSolve() {
      const self = this as unknown as {
        answerOptions: IRawAnswerOption[];
        exercise: { playWordsCount?: number };
      };
      const playWordsCount = self.exercise.playWordsCount ?? 1;
      return [
        ...shuffleArray(self.answerOptions, 1),
        ...shuffleArray(self.answerOptions, 2),
        ...shuffleArray(self.answerOptions, 3),
      ].map((item, index) => {
        let answers: IRawAnswerOption[] = [];
        if (playWordsCount === 1) {
          answers.push(item);
        } else {
          const refs = [
            ...shuffleArray(self.answerOptions, 4),
            ...shuffleArray(self.answerOptions, 5),
            ...shuffleArray(self.answerOptions, 6),
          ];
          answers = refs.slice(0, playWordsCount);
        }
        return {
          answer: answers,
          order: index,
        };
      });
    },
  },
};

export type TaskEnvironmentalSounds = TaskBase & WithLegacy<{
  answerOptions: IRawAnswerOption[];
  exerciseMechanism: ExerciseMechanism;
  tasksToSolve: { answer: IRawAnswerOption[]; order: number }[];
  [Type]: 'task/environmental-sounds';
}>;
