import DS from 'ember-data';
const { attr } = DS;
import BaseTask from '../task';

export default class TaskSentenceModel extends BaseTask.extend({
  template: attr('string'),
  answerOptions: attr(),
  correctAnswer: attr('string'),
  exerciseType: 'sentence',
  answerParts: attr('array'),
}) {}
