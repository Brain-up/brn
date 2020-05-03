import { attr } from '@ember-data/model';
import BaseTask from '../task';

export default class SingleWordTask extends BaseTask.extend({
  word: attr('string'),
  audioFileUrl: attr('string'),
  pictureFileUrl: attr('string'),
  words: attr('array'),
  exerciseType: 'single-words',
}) {}
