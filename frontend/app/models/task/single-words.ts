import { attr } from '@ember-data/model';
import BaseTask from '../task';

export default class SingleWordTask extends BaseTask {
  @attr('string') word!: string;
  @attr('string') audioFileUrl!: string;
  @attr('string') pictureFileUrl!: string;
  @attr('array') words!: string[];
  exerciseType = 'single-words';
}
