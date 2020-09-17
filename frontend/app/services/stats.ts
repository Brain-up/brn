import Service from '@ember/service';
import Exercise from 'brn/models/exercise';
export enum  StatEvents {
  Start = 'start',
  Finish = 'finish',
  WrongAnswer = 'wrongAnswer',
  Task = 'task',
  Repeat  = 'repeat',
  RightAnswer = 'rightAnswer'
}
export default class StatsService extends Service {
  stats = new WeakMap();
  lastModel: Exercise | null = null;
  emptyStats() {
    return {
      startTime: null,
      endTime:  null,
      wrongAnswersCount: 0,
      rightAnswersCount: 0,
      repeatsCount: 0,
      tasksCount: 0
    }
  }
  statsFor(model: Exercise) {
    return this.stats.get(model);
  }
  registerModel(model: Exercise) {
    this.stats.set(model, this.emptyStats());
    this.lastModel = model;
  }
  addEvent(eventName: StatEvents) {
    if (this.lastModel === null) {
      return;
    }
    const item = this.statsFor(this.lastModel);
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
    }
  }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  interface Registry {
    'stats': StatsService;
  }
}
