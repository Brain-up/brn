import BaseTask from '../task';
import { attr } from '@ember-data/model';
import { cached } from 'tracked-toolbox';
import shuffleArray from 'brn/utils/shuffle-array';
export default class TaskPhraseModel extends BaseTask {
  exerciseType = 'phrase'
  @attr() declare answerOptions;
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
