import { belongsTo, hasMany, attr } from '@ember-data/model';
import CompletionDependent from './completion-dependent';
import { reads } from '@ember/object/computed';
import { computed } from '@ember/object';
import arrayPreviousItems from 'brn/utils/array-previous-items';
import { inject as service } from '@ember/service';
import { IStatsExerciseStats } from "brn/services/stats";

interface IStatsObject {
  countedSeconds: 61,
  endTime: "2020-09-17T12:33:48.649Z",
  exerciseId: "110",
  listeningsCount: 15,
  repetitionIndex: 33.33333333333333,
  rightAnswersCount: 12,
  rightAnswersIndex: 100,
  startTime: "2020-09-17T12:32:39.192Z",
  tasksCount: 9,
  userId: 3
}

export default class Exercise extends CompletionDependent.extend({
  session: service('session'),
  name: attr('string'),
  available: attr('boolean'),
  description: attr('string'),
  level: attr('number'),
  pictureUrl: attr('string'),
  order: attr('number'),
  exerciseType: attr('string'),
  series: belongsTo('series', { async: true }),
  tasks: hasMany('task', { async: true }),
  children: reads('tasks'),
  parent: reads('series'),
  startTime: attr('date'),
  endTime: attr('date'),
  noise: attr(''),
  get noiseLevel() {
    return this.noise?.level || 0;
  },
  get noiseUrl() {
    return this.noise?.url || null;
  },
  sortedTasks: reads('sortedChildren'),
  // eslint-disable-next-line ember/require-computed-property-dependencies
  previousSiblings: computed('series.groupedByNameExercises', function () {
    return arrayPreviousItems(
      this,
      // eslint-disable-next-line ember/no-get
      this.get('series.groupedByNameExercises')[this.name],
    );
  }),
  // eslint-disable-next-line ember/require-computed-property-dependencies
  isCompleted: computed(
    'tasks.@each.isCompleted',
    'previousSiblings.@each.isCompleted',
    'tasksManager.completedTasks.[]',
    function () {
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
  siblingExercises: computed('series.sortedExercises.[]', function () {
    return this.series.get('sortedExercises') || [];
  }),
  nextSiblings: computed('siblingExercises.[]', function () {
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
    const { startTime, endTime, id } = this;

    return {
      startTime,
      endTime,
      exerciseId: id
    };
  },
  calcStats(data: IStatsExerciseStats): IStatsObject {
    const { stats } = this;
    stats.tasksCount = data.rightAnswersCount - data.repeatsCount;
    stats.rightAnswersCount = data.rightAnswersCount;
    stats.listeningsCount = data.playsCount;
    stats.countedSeconds = data.countedSeconds;
    stats.repetitionIndex = (data.repeatsCount / stats.tasksCount) * 100;
    if (isNaN(stats.repetitionIndex)) {
      stats.repetitionIndex = 0;
    } else {
      stats.repetitionIndex = Number(stats.repetitionIndex.toFixed(2));
    }
    stats.rightAnswersIndex = ((data.rightAnswersCount - data.repeatsCount) / stats.tasksCount) * 100;
    if (isNaN(stats.rightAnswersIndex)) {
      stats.rightAnswersIndex = 0;
    } else {
      stats.rightAnswersIndex = Number(stats.rightAnswersIndex.toFixed(2)) - stats.repetitionIndex;
    }

    return stats;
  },
  async postHistory(data: IStatsExerciseStats) {
    const stats: IStatsObject = this.calcStats(data);

    await fetch('/api/study-history', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        ...stats,
        // eslint-disable-next-line ember/no-get
        userId: this.get('session.data.user.id') || null
      }),
    });
  },
}) { }
