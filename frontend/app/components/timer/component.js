import Ember from 'ember';
import Component from '@ember/component';
import { computed } from '@ember/object';
import { inject } from '@ember/service';
import { reads } from '@ember/object/computed';
import { later, cancel } from '@ember/runloop';

export default Component.extend({
  init() {
    this._super(...arguments);
  },
  willDestroyElement() {
    this._super(...arguments);
    this.stopTimer();
  },
  didInsertElement() {
    this._super(...arguments);
    this.studyingTimer.register(this);
    this.set('isStarted', false);
  },
  studyingTimer: inject(),
  countedSeconds: reads('studyingTimer.countedSeconds'),
  isPaused: reads('studyingTimer.isPaused'),
  isStarted: false,
  timer: null,
  displayValue: computed('countedSeconds', function() {
    const mins = Math.floor(this.countedSeconds / 60);
    const seconds = this.countedSeconds % 60;
    return `${leadingZero(mins)}:${leadingZero(seconds)}`;
  }),

  updateSecondsCount() {
    if (!this.isPaused) {
      const secondsPassed = Math.floor(
        (new Date().getTime() - this.timeStart) / 1000,
      );
      this.studyingTimer.setTime(secondsPassed);
    }
    this.set('timerId', later(this, this.updateSecondsCount, 1000));
  },

  setStartTime() {
    this.set('timeStart', new Date().getTime() - this.countedSeconds * 1000);
  },

  startTimer() {
    this.setStartTime();
    this.set('isStarted', true);
    Ember.testing
      ? ''
      : this.set('timerId', later(this, this.updateSecondsCount, 1000));
  },

  stopTimer() {
    cancel(this.timerId);
  },

  runTimer() {
    if (!this.isStarted) {
      this.startTimer();
    } else {
      this.studyingTimer.resume();
      this.setStartTime();
    }
  },
});

function leadingZero(timeItem) {
  return timeItem.toString().length === 1 ? `0${timeItem}` : timeItem;
}
