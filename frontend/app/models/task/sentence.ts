import { attr } from '@ember-data/model';
import BaseTask from '../task';

export default class TaskSentenceModel extends BaseTask {
  exerciseType = 'sentence';

  get selectedItemsOrder() {
    return this.template
      .split('<')[1]
      .split('>')[0]
      .split(' ');
  }

  @attr() answerOptions!: any;
  @attr('string') correctAnswer!: string;
  @attr('string') template!: string;
  @attr('array') answerParts!: any;
}
