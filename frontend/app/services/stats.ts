import Service, { inject as service } from '@ember/service';
import Exercise from 'brn/models/exercise';
import StudyingTimerService from './studying-timer';
export enum  StatEvents {
  Start = 'start',
  Finish = 'finish',
  WrongAnswer = 'wrongAnswer',
  Task = 'task',
  Repeat  = 'repeat',
  RightAnswer = 'rightAnswer',
  PlayAudio = 'play'
}

export interface IStatsExerciseStats {
  startTime: Date | null;
  endTime: Date | null;
  wrongAnswersCount: number,
  playsCount: number,
  rightAnswersCount: number,
  repeatsCount: number,
  countedSeconds: number,
  tasksCount: number
}

export default class StatsService extends Service {
  @service('studying-timer') studyingTimer!: StudyingTimerService
  stats = new WeakMap();
  lastModel: Exercise | null = null;
  emptyStats() {
    return {
      startTime: null,
      endTime:  null,
      wrongAnswersCount: 0,
      countedSeconds: 0,
      rightAnswersCount: 0,
      repeatsCount: 0,
      tasksCount: 0,
      playsCount: 0
    }
  }
  statsFor(model: Exercise): IStatsExerciseStats {
    return this.stats.get(model);
  }
  registerModel(model: Exercise) {
    this.stats.set(model, this.emptyStats());
    this.lastModel = model;
  }
  unregisterModel(model: Exercise) {
    this.stats.set(model, this.emptyStats());
  }
  addEvent(eventName: StatEvents) {
    if (this.lastModel === null) {
      return;
    }
    const item = this.statsFor(this.lastModel);

    item.countedSeconds = this.studyingTimer.countedSeconds;

    if (eventName === StatEvents.Start) {
      item.startTime = new Date();
    } else if (eventName === StatEvents.Finish) {
      item.endTime = new Date();
    } else if (eventName === StatEvents.WrongAnswer) {
      item.wrongAnswersCount++;
    } else if (eventName === StatEvents.Task) {
      item.tasksCount++;
    } else if (eventName === StatEvents.Repeat) {
      item.repeatsCount++;
    } else if (eventName === StatEvents.RightAnswer) {
      item.rightAnswersCount++;
    } else if (eventName === StatEvents.PlayAudio) {
      item.playsCount++;
    }
  }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  interface Registry {
    'stats': StatsService;
  }
}
