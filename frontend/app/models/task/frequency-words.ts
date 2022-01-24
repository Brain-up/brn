import { attr } from '@ember-data/model';
import BaseTask from '../task';
import shuffleArray from 'brn/utils/shuffle-array';
import { cached } from 'tracked-toolbox';

export default class TaskFrequencyWordsModel extends BaseTask {
  @attr() answerOptions!: any;
  exerciseType = 'single-simple-words';
  @cached
  get tasksToSolve() {
    return [
      ...shuffleArray(this.answerOptions, 1),
      ...shuffleArray(this.answerOptions, 2),
      ...shuffleArray(this.answerOptions, 3),
    ].map((item, index) => {
      return {
        answer: [item],
        order: index,
      };
    });
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    'task/frequency-words': TaskFrequencyWordsModel;
  }
}
