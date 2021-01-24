import { belongsTo, hasMany, attr } from '@ember-data/model';
import CompletionDependent from './completion-dependent';
import arrayPreviousItems from 'brn/utils/array-previous-items';
import { inject as service } from '@ember/service';
import { IStatsExerciseStats } from "brn/services/stats";
import Session from 'ember-simple-auth/services/session';
import SeriesModel from './series';
import SignalModel from './signal';
import TaskModel from 'brn/models/task';
import { cached } from 'tracked-toolbox';
import SubgroupModel from './subgroup';

export interface IStatsObject {
  countedSeconds: number,
  endTime: Date,
  exerciseId: "110",
  listeningsCount: number,
  repetitionIndex: number,
  rightAnswersCount: number,
  rightAnswersIndex: number,
  startTime: Date,
  tasksCount: number,
}

interface IStatsSaveDTO {
  exerciseId: number,
  startTime: Date,
  endTime: Date,
  executionSeconds: number,
  tasksCount: number,
  replaysCount: number,
  wrongAnswers: number
}
export default class Exercise extends CompletionDependent  {
  @service('session') session!: Session;
  @attr('string') name!: string;
  @attr('boolean') available!: boolean;
  @attr('string') description!: string;
  @attr('number') level!: number;
  @attr('string') pictureUrl!: string;
  @attr('number') order!: number;
  // @todo - add enum
  @attr('string') exerciseType!: string;
  @belongsTo('series', { async: true }) series!: SeriesModel;
  @hasMany('signal', { async: false }) signals!: SignalModel[];
  @hasMany('task', { async: true }) tasks!: TaskModel[];
  // @ts-ignore
  get children() {
    return this.tasks;
  }
  @belongsTo('subgroup', { async: false, inverse: 'exercises' }) parent!: SubgroupModel;
  @attr('date') startTime!: Date;
  @attr('date') endTime!: Date;
  @attr() noise!: {
    level?: number,
    url?: string
  }
  get noiseLevel() {
    return this.noise?.level || 0;
  }
  get noiseUrl() {
    return this.noise?.url || null;
  }
  get sortedTasks() {
    return this.sortedChildren;
  }
  @cached
  get previousSiblings() {
    return arrayPreviousItems(
      this,
      // @ts-ignore
      this.get('series.groupedByNameExercises')[this.name],
    );
  }
  @cached
  get isCompleted() {
    // @ts-ignore
    const tasksIds = this.hasMany('tasks').ids();
    const completedTaskIds = this.tasksManager.completedTasks.mapBy('id');
    const tasksCompleted = tasksIds.every((taskId) =>
      completedTaskIds.includes(taskId),
    );
    return (
      (!tasksIds.length && (this.isFirst || this.canInteract)) ||
      tasksCompleted
    );
  }
  get siblingExercises() {
    return this.series.get('sortedExercises') || [];
  };
  get nextSiblings() {
    return this.siblingExercises.slice(this.siblingExercises.indexOf(this) + 1);
  };
  get isStarted() {
    return this.startTime && !this.endTime;
  };
  get stats() {
    const { startTime, endTime, id } = this;

    return {
      startTime,
      endTime,
      exerciseId: id
    };
  }
  trackTime(type = 'start') {
    if (type === 'start' || type === 'end') {
      // @ts-expect-error
      this.set(`${type}Time`, new Date());
    }
  }
  calcStats(data: IStatsExerciseStats | undefined): IStatsObject {
    if (!data) {
      throw new Error('unable calculate exercise stats');
    }
    const { stats }: { stats: any } = this;
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
  }
  async postHistory(data: IStatsExerciseStats) {
    const stats: IStatsObject = this.calcStats(data);
    const newStats: IStatsSaveDTO = {
      endTime: stats.endTime,
      startTime: stats.startTime,
      executionSeconds: stats.countedSeconds,
      exerciseId: parseInt(this.id, 10),
      replaysCount: data.repeatsCount,
      wrongAnswers: data.wrongAnswersCount,
      tasksCount: data.rightAnswersCount
    }

    await fetch('/api/study-history', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(newStats),
    });
  }
}


