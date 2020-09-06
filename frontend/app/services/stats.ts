import Service from '@ember/service';

export default class StatsService extends Service {
  stats = new WeakMap();
  lastModel = null;
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
  statsFor(model) {
    return this.stats.get(model);
  }
  registerModel(model) {
    this.stats.set(model, this.emptyStats());
    this.lastModel = model;
  }
  addEvent(eventName) {
    const item = this.statsFor(this.lastModel);
    if (!item) {
      return;
    }
    if (eventName === 'start') {
      item.startTime = new Date();
    } else if (eventName === 'finish') {
      item.endTime = new Date();
    } else if (eventName === 'wrongAnswer') {
      item.wrongAnswersCount++;
    } else if (eventName === 'task') {
      item.tasksCount++;
    } else if (eventName === 'repeat') {
      item.repeatsCount++;
    } else if (eventName === 'rightAnswer') {
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
