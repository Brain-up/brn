import { belongsTo, hasMany, attr } from '@ember-data/model';
import CompletionDependent from './completion-dependent';
import { reads } from '@ember/object/computed';
import { computed } from '@ember/object';
import arrayPreviousItems from 'brn/utils/array-previous-items';
import { inject as service } from '@ember/service';

export default class Exercise extends CompletionDependent.extend({
  session: service('session'),
  name: attr('string'),
  available: attr('boolean'),
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
  previousSiblings: computed('series.groupedByNameExercises', function() {
    return arrayPreviousItems(
      this,
      this.get('series.groupedByNameExercises')[this.name],
    );
  }),
  isCompleted: computed(
    'tasks.@each.isCompleted',
    'previousSiblings.@each.isCompleted',
    'tasksManager.completedTasks.[]',
    function() {
      const tasksIds = this.hasMany('tasks').ids();
      const completedTaskIds = this.tasksManager.completedTasks.mapBy('id');
      const tasksCompleted = tasksIds.every((taskId) =>
        completedTaskIds.includes(taskId),
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
  get isStarted() {
    return this.startTime && !this.endTime;
  },
  trackTime(type = 'start') {
    if (type === 'start' || type === 'end') {
      this.set(`${type}Time`, new Date());
    }
  },
  get stats() {
    const { startTime, endTime, tasks, id } = this;
    const items = tasks.toArray();

    const repetitionsCount = items.reduce((result, task) => {
      if (task.repetitionCount) {
        result += task.repetitionCount;
      }
      return result;
    }, 0);

    const tasksCount = items.reduce((result, task) => {
      if (task.tasksToSolve) {
        return result + task.tasksToSolve.length;
      } else {
        return result + 1;
      }
    }, 0);

    const repetitionRatio = repetitionsCount / tasksCount;
    const repetitionIndex =  !isFinite(repetitionRatio) || isNaN(repetitionRatio) ? 0 : repetitionRatio.toFixed(2);

    return {
      startTime,
      endTime,
      repetitionIndex,
      exerciseId: id,
      tasksCount
    };
  },
  async postHistory() {
    const { stats } = this;
    await fetch('/api/study-history', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        ...stats,
        userId: this.get('session.data.user.id') || null
      }),
    });
  },
}) {}
