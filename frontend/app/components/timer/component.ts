import Ember from 'ember';
import Component from '@glimmer/component';
import { action } from '@ember/object';
import { inject  as service } from '@ember/service';
import { later, cancel } from '@ember/runloop';
import StudyingTimerService from 'brn/services/studying-timer';
import { tracked } from '@glimmer/tracking';

interface ITimerComponentArgs {
  hideControls?: boolean
}

export default class TimerComponent extends Component<ITimerComponentArgs> {
  @service('studying-timer') studyingTimer !: StudyingTimerService;

  @action
  onInsert() {
    this.studyingTimer.register(this);
  }

  @action
  onDestroy() {
    this.studyingTimer.unregister(this);
    this.stopTimer();
  }

  get countedSeconds() {
    return this.studyingTimer.countedSeconds;
  }

  get isPaused() {
    return this.studyingTimer.isPaused;
  }

  @tracked
  isStarted = false;

  @tracked
  timer = null;

  timerId: any = null;

  get displayValue() {
    const mins = Math.floor(this.countedSeconds / 60);
    const hours = Math.floor(mins / 60);
    const seconds = this.countedSeconds % 60;
    return `${
      hours ? leadingZero(hours) + ':' : ''
    }${leadingZero(mins - hours * 60)}:${leadingZero(seconds)}`;
  }

  updateSecondsCount() {
    if (!this.isPaused) {
      this.studyingTimer.addTime(1);
    }
    this.timerId = later(this, this.updateSecondsCount, 1000);
  }

  setStartTime() {
    if (this.isDestroyed || this.isDestroying) {
      return;
    }
  }

  startTimer() {
    this.setStartTime();
    this.isStarted = true;
    if (!Ember.testing) {
      this.timerId =  later(this, this.updateSecondsCount, 1000);
    }
  }

  stopTimer() {
    cancel(this.timerId);
  }

  runTimer() {
    this.isStarted ? this.relaunchStartedTimer() : this.startTimer();
  }

  relaunchStartedTimer() {
    this.studyingTimer.resume();
    this.setStartTime();
  }
}

function leadingZero(timeItem: number) {
  return timeItem.toString().length === 1 ? `0${timeItem}` : timeItem;
}
