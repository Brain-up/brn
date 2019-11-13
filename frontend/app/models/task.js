import DS from 'ember-data';
const { attr, belongsTo } = DS;
import { tag } from 'ember-awesome-macros';
import { computed } from '@ember/object';
import { inject as service } from '@ember/service';
import CompletionDependent from './completion-dependent';
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

  savePassed() {
    return this.tasksManager.saveAsCompleted(this);
  },

  audioFileUrl: tag`/audio/${'audioFileId'}`,
}) {}
