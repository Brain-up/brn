import DS from 'ember-data';
const { attr } = DS;
import BaseTask from '../task';

export default class SingleWords extends BaseTask.extend({
  word: attr('string'),
  audioFileUrl: attr('string'),
  pictureFileUrl: attr('string'),
  words: attr('array'),
}) {}
