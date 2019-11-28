import DS from 'ember-data';
const { attr, belongsTo } = DS;
import { isEmpty } from 'ember-awesome-macros';
import { computed } from '@ember/object';
import { inject as service } from '@ember/service';
import CompletionDependent from './completion-dependent';
import arrayNext from 'brn/utils/array-next';
import { reads } from '@ember/object/computed';

export default class Task extends CompletionDependent.extend({
  name: attr('string'),
  word: attr('string'),
  order: attr('number'),
  audioFileUrl: attr('string'),
  words: attr('array'),
  exercise: belongsTo('exercise', { async: true }),
  tasksManager: service(),
  parent: reads('exercise'),
  isCompleted: computed('tasksManager.completedTasks.[]', function() {
    return this.tasksManager.isCompleted(this);
  }),
  nextTaskSameExersise: computed('exercise.tasks.[]', function() {
    return arrayNext(this, this.exercise.content.get('sortedChildren'));
  }),
  firstTaskNextExersise: computed('exercise.tasks.[]', function() {
    const nextExercises = this.exercise.get('nextSiblings');
    return nextExercises.mapBy('tasks.firstObject').filter(Boolean)[0];
  }),
  nextTask: computed('exercise.tasks.[]', function() {
    return this.nextTaskSameExersise || this.firstTaskNextExersise;
  }),

  isLastTask: isEmpty('nextTask'),

  nextAttempt: false,

  savePassed() {
    return this.tasksManager.saveAsCompleted(this);
  },
}) {}
