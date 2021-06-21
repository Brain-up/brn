import { attr } from '@ember-data/model';
import BaseTask from '../task';
import shuffleArray from 'brn/utils/shuffle-array';
import { cached } from 'tracked-toolbox';

export default class TaskSingleSimpleWordsModel extends BaseTask {
  @attr() answerOptions!: any;
  exerciseType = 'single-simple-words';
  @cached
  get tasksToSolve() {
    return [
      ...shuffleArray(this.answerOptions),
      ...shuffleArray(this.answerOptions),
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
    'task/single-simple-words': TaskSingleSimpleWordsModel;
  }
}
