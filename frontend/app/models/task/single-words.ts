import { attr } from '@ember-data/model';
import BaseTask from '../task';

export default class SingleWordTask extends BaseTask {
  @attr('string') word!: string;
  @attr('string') audioFileUrl!: string;
  @attr('string') pictureFileUrl!: string;
  @attr('array') words!: string[];
  exerciseType = 'single-words';
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    'task/single-words': SingleWordTask;
  }
}
