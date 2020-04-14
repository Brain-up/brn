import DS from 'ember-data';
const { attr, hasMany, belongsTo } = DS;
import CompletionDependent from './completion-dependent';
import { reads } from '@ember/object/computed';
import { computed } from '@ember/object';
import arrayPreviousItems from 'brn/utils/array-previous-items';

export default class Exercise extends CompletionDependent.extend({
  name: attr('string'),
  description: attr('string'),
  level: attr('number'),
  order: attr('number'),
  exerciseType: attr('string'),
  series: belongsTo('series', { async: true }),
  tasks: hasMany('task', { async: true }),
  children: reads('tasks'),
  parent: reads('series'),
  startTime: attr('date'),
  endTime: attr('date'),
  sortedTasks: reads('sortedChildren'),
  hideExerciseNavigation: computed(function() {
    return this.exerciseType === 'WORDS_SEQUENCES';
  }),
  previousSiblings: computed('series.groupedByNameExercises', function() {
    return arrayPreviousItems(
      this,
      this.get('series.groupedByNameExercises')[this.name],
    );
  }),
  isCompleted: computed(
    'tasks.@each.isCompleted',
    'previousSiblings.@each.isCompleted',
    function() {
      const tasksIds = this.hasMany('tasks').ids();
      const completedTaskIds = this.tasksManager.completedTasks.mapBy('id');
      const tasksCompleted = tasksIds.every(
        (taskId) => completedTaskIds.includes(taskId),
      );
      return (
        (!tasksIds.length && (this.isFirst || this.canInteract)) ||
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
  async postHistory() {
    const { startTime, endTime, tasks, id } = this;

    const repetitionsCount = tasks.reduce((result, task) => {
      if (task.repetitionCount) {
        result += task.repetitionCount;
      }
      return result;
    }, 0);

    const repetitionIndex = repetitionsCount / tasks.length;

    await fetch('/api/study-history', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        startTime,
        endTime,
        repetitionIndex,
        exerciseId: id,
        tasksCount: tasks.length,
        userId: 2, //temporary
      }),
    });
  },
}) {}
