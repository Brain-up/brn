import DS from 'ember-data';
const { attr, belongsTo } = DS;
import { tag, isEmpty } from 'ember-awesome-macros';
import { computed } from '@ember/object';
import { inject as service } from '@ember/service';
import CompletionDependent from './completion-dependent';
import arrayNext from 'brn/utils/array-next';
import { reads } from '@ember/object/computed';

export default class Task extends CompletionDependent.extend({
  name: attr('string'),
  word: attr('string'),
  order: attr('number'),
  audioFileId: attr('string'),
  words: attr('array'),
  exercise: belongsTo('exercise', { async: true }),
  tasksManager: service(),
  parent: reads('exercise'),
  isCompleted: computed('tasksManager.completedTasks.[]', function() {
    return this.tasksManager.isCompleted(this);
  }),
  nextTaskSameExersise: computed(function() {
    return arrayNext(this, this.exercise.get('tasks'));
  }),
  firstTaskNextExersise: computed(function() {
    const nextExercise = arrayNext(
      this.exercise.get('content'),
      this.exercise.get('series.exercises'),
    );
    return nextExercise && nextExercise.get('tasks').toArray()[0];
  }),
  nextTask: computed(function() {
    return this.nextTaskSameExersise || this.firstTaskNextExersise;
  }),

  isLastTask: isEmpty('nextTask'),

  savePassed() {
    return this.tasksManager.saveAsCompleted(this);
  },

  audioFileUrl: tag`/audio/${'audioFileId'}`,
}) {}
