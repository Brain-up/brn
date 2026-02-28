import { withDefaults, type WithLegacy } from '@warp-drive/legacy/model/migration-support';
import { Type } from '@warp-drive/core/types/symbols';
import type { LegacyResourceSchema } from '@warp-drive/core/types/schema/fields';
import type { CAUTION_MEGA_DANGER_ZONE_Extension } from '@warp-drive/core/reactive';
import { BASE_TASK_FIELDS, LOCAL_TASK_FIELDS, type TaskBase } from '../task';
import shuffleArray from 'brn/utils/shuffle-array';
import deepCopy from 'brn/utils/deep-copy';
import { ExerciseMechanism } from 'brn/utils/exercise-types';

function createTasks(
  [first, ...tail]: Array<string[]>,
  acc: Array<string[]> = [],
): Array<string[]> {
  const results: Array<string[]> = [];
  const finalResults: Array<string[]> = [];

  first.forEach((i) => {
    results.push([i]);
  });

  acc.forEach((row) => {
    results.forEach((result) => {
      finalResults.push(row.concat(result));
    });
  });
  if (tail.length) {
    return createTasks(tail, acc.length ? finalResults : results);
  }

  return acc.length ? finalResults : results;
}

export const TaskWordsSequencesSchema: LegacyResourceSchema = withDefaults({
  type: 'task/words-sequences',
  fields: [
    ...BASE_TASK_FIELDS,
    { kind: 'attribute', name: 'template', type: 'string' },
    {
      kind: 'attribute',
      name: 'wrongAnswers',
      type: 'array',
    },
  ],
  objectExtensions: ['task-ext', 'task-words-sequences-ext'],
}) as LegacyResourceSchema;

// Add tracked local fields
TaskWordsSequencesSchema.fields.push(...LOCAL_TASK_FIELDS);

export const TaskWordsSequencesExtension: CAUTION_MEGA_DANGER_ZONE_Extension = {
  kind: 'object',
  name: 'task-words-sequences-ext',
  features: {
    get exerciseMechanism() {
      return ExerciseMechanism.MATRIX;
    },

    get selectedItemsOrder(): string[] {
      const self = this as unknown as { template: string };
      return self.template.split('<')[1].split('>')[0].split(' ');
    },

    get possibleTasks(): Array<string[]> {
      const self = this as unknown as {
        answerOptions: Record<string, string[]>;
        selectedItemsOrder: string[];
      };
      const options = Object.keys(self.answerOptions);
      const taskPartsOptions = self.selectedItemsOrder
        .filter((key) => options.includes(key))
        .map((orderItemName) => self.answerOptions[orderItemName] || []);
      return shuffleArray(createTasks(taskPartsOptions), 10);
    },

    get doubledTasks(): string[][] {
      const self = this as unknown as { possibleTasks: Array<string[]> };
      return ([] as string[][]).concat(
        deepCopy(self.possibleTasks),
        shuffleArray(deepCopy(self.possibleTasks), 3),
      );
    },

    get tasksSequence(): { answer: string[]; order: number }[] {
      const self = this as unknown as { doubledTasks: string[][] };
      return shuffleArray(self.doubledTasks, 10).map(
        (item: string[], index: number) => {
          return {
            answer: [...item],
            order: index,
          };
        },
      );
    },

    get tasksToSolve(): { answer: string[]; order: number }[] {
      const self = this as unknown as {
        tasksSequence: { answer: string[]; order: number }[];
        wrongAnswers: { answer: string[]; order: number }[];
      };
      return shuffleArray(self.tasksSequence, 10)
        .concat(
          self.wrongAnswers.map((wrongAnswer, index: number) => {
            return {
              ...wrongAnswer,
              order: self.tasksSequence.length + index,
            };
          }),
        )
        .slice(0, 30);
    },
  },
};

export type TaskWordsSequences = TaskBase & WithLegacy<{
  template: string;
  answerOptions: Record<string, string[]>;
  wrongAnswers: unknown[];
  exerciseMechanism: ExerciseMechanism;
  selectedItemsOrder: string[];
  possibleTasks: Array<string[]>;
  doubledTasks: string[][];
  tasksSequence: { answer: string[]; order: number }[];
  tasksToSolve: { answer: string[]; order: number }[];
  [Type]: 'task/words-sequences';
}>;
