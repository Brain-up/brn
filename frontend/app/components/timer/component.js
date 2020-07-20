import Ember from 'ember';
import Component from '@ember/component';
import { computed } from '@ember/object';
import { inject } from '@ember/service';
import { reads } from '@ember/object/computed';
import { later, cancel } from '@ember/runloop';

export default Component.extend({
  tagName: '',

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
    const hours = Math.floor(mins / 60);
    const seconds = this.countedSeconds % 60;
    return `${
      hours ? leadingZero(hours) + ':' : ''
    }${leadingZero(mins - hours * 60)}:${leadingZero(seconds)}`;
  }),

  updateSecondsCount() {
    if (!this.isPaused) {
      this.studyingTimer.addTime(1);
    }
    this.set('timerId', later(this, this.updateSecondsCount, 1000));
  },

  setStartTime() {
    if (this.isDestroyed || this.isDestroying) {
      return;
    }
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
    this.isStarted ? this.relaunchStartedTimer() : this.startTimer();
  },

  relaunchStartedTimer() {
    this.studyingTimer.resume();
    this.setStartTime();
  },
});

function leadingZero(timeItem) {
  return timeItem.toString().length === 1 ? `0${timeItem}` : timeItem;
}
