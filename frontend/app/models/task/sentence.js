import DS from 'ember-data';
const { attr } = DS;
import BaseTask from '../task';
import { computed } from '@ember/object';

export default class TaskSentenceModel extends BaseTask.extend({
  template: attr('string'),
  selectedItemsOrder: computed('template', function() {
    return this.template
      .split('<')[1]
      .split('>')[0]
      .split(' ');
  }),
  answerOptions: attr(),
  correctAnswer: attr('string'),
  exerciseType: 'sentence',
  answerParts: attr('array'),
  answerTypes: computed('answerOptions', function() {
    return Object.keys(this.answerOptions);
  }),
}) {}
