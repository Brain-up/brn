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
  @attr('array') answerParts!: unknown[];
}


// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    'task/sentence': TaskSentenceModel;
  }
}
