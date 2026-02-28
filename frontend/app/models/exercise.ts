import { belongsTo, hasMany, attr, type HasMany } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import CompletionDependent from './completion-dependent';
import arrayPreviousItems from 'brn/utils/array-previous-items';
import { inject as service } from '@ember/service';
import { IStatsExerciseStats } from 'brn/services/stats';
import Session from 'ember-simple-auth/services/session';
import SeriesModel from './series';
import SignalModel from './signal';
import TaskModel from 'brn/models/task';
import { cached } from 'tracked-toolbox';
import SubgroupModel from './subgroup';
import NetworkService from 'brn/services/network';

export interface IStatsObject {
  countedSeconds: number;
  endTime: Date;
  exerciseId: '110';
  listeningsCount: number;
  repetitionIndex: number;
  rightAnswersCount: number;
  rightAnswersIndex: number;
  startTime: Date;
  tasksCount: number;
}

interface IStatsSaveDTO {
  exerciseId: number;
  startTime: Date;
  endTime: Date;
  executionSeconds: number;
  tasksCount: number;
  replaysCount: number;
  wrongAnswers: number;
}
export default class Exercise extends CompletionDependent {
  declare [Type]: 'exercise';
  @service('session') session!: Session;
  @service('network') network!: NetworkService;
  @attr('string') name!: string;
  @attr('boolean') available!: boolean;
  @attr('number', { defaultValue: 1 }) playWordsCount!: number;
  @attr('number', { defaultValue: 3 }) wordsColumns!: number;
  @attr('string') description!: string;
  @attr('number') level!: number;
  @attr('string') pictureUrl!: string;
  @attr('number') order!: number;
  // @todo - add enum
  @attr('string') exerciseType!: string;
  @belongsTo('series', { async: false, inverse: 'exercises' }) series!: SeriesModel;
  @hasMany('signal', { async: false, inverse: null }) signals!: SignalModel[];
  @hasMany('task', { async: false, inverse: 'exercise', polymorphic: true })
  tasks!: HasMany<TaskModel>;
  // @ts-expect-error overridden property
  get children() {
    return this.tasks;
  }
  @belongsTo('subgroup', { async: false, inverse: 'exercises' })
  parent!: SubgroupModel;
  @attr('date') startTime!: Date;
  @attr('date') endTime!: Date;
  @attr() noise!: {
    level?: number;
    url?: string;
  };
  @attr('boolean') audioFileUrlGenerated!: boolean;
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
      this.series?.groupedByNameExercises?.[this.name],
    );
  }
  @cached
  get isCompleted(): boolean {
    const tasksIds: string[] = (this as any).hasMany('tasks').ids();
    const completedTaskIds = this.tasksManager.completedTasks.map((t: { id: string }) => t.id);
    const tasksCompleted = tasksIds.every((taskId: string) =>
      completedTaskIds.includes(taskId),
    );
    return (
      (!tasksIds.length && (this.isFirst || this.canInteract)) || tasksCompleted
    );
  }
  get siblingExercises() {
    return this.series?.sortedExercises || [];
  }
  get nextSiblings() {
    return this.siblingExercises.slice(this.siblingExercises.indexOf(this) + 1);
  }
  get isStarted() {
    return this.startTime && !this.endTime;
  }
  get stats() {
    const { startTime, endTime, id } = this;

    return {
      startTime,
      endTime,
      exerciseId: id,
    };
  }
  trackTime(type = 'start') {
    if (type === 'start') {
      this.startTime = new Date();
    } else if (type === 'end') {
      this.endTime = new Date();
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
    stats.rightAnswersIndex =
      ((data.rightAnswersCount - data.repeatsCount) / stats.tasksCount) * 100;
    if (isNaN(stats.rightAnswersIndex)) {
      stats.rightAnswersIndex = 0;
    } else {
      stats.rightAnswersIndex =
        Number(stats.rightAnswersIndex.toFixed(2)) - stats.repetitionIndex;
    }

    return stats;
  }
  async postHistory(data: IStatsExerciseStats) {
    const stats: IStatsObject = this.calcStats(data);
    const newStats: IStatsSaveDTO = {
      endTime: stats.endTime,
      startTime: stats.startTime,
      executionSeconds: stats.countedSeconds,
      exerciseId: parseInt(this.id!, 10),
      replaysCount: data.repeatsCount,
      wrongAnswers: data.wrongAnswersCount,
      tasksCount: data.rightAnswersCount,
    };

    await this.network.postRequest('study-history', newStats);
  }
}

