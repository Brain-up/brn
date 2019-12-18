import DS from 'ember-data';
const { attr, hasMany, belongsTo } = DS;
import CompletionDependent from './completion-dependent';
import { reads } from '@ember/object/computed';
import { computed } from '@ember/object';

export default class Exercise extends CompletionDependent.extend({
  name: attr('string'),
  description: attr('string'),
  order: attr('number'),
  series: belongsTo('series', { async: true }),
  tasks: hasMany('task', { async: true }),
  children: reads('tasks'),
  parent: reads('series'),
  startTime: attr('date'),
  endTime: attr('date'),
  sortedTasks: reads('sortedChildren'),
  isCompleted: computed(
    'tasks.@each.isCompleted',
    'previousSiblings.@each.isCompleted',
    function() {
      const tasksCompleted = this.get('tasks').every(
        (task) => task.isCompleted,
      );
      return (
        (!this.tasks.length && (this.isFirst || this.canInteract)) ||
        tasksCompleted
      );
    },
  ),
  siblingExercises: computed('series.sortedExercises.[]', function() {
    return this.series.get('sortedExercises') || [];
  }),
  nextSiblings: computed('siblingExercises.[]', function() {
    return this.siblingExercises.slice(this.siblingExercises.indexOf(this) + 1);
  }),
  trackTime(type = 'start') {
    if (type === 'start' || type === 'end') {
      this.set(`${type}Time`, new Date());
    }
  },
}) {}
