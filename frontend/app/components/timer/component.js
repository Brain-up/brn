import Component from '@ember/component';
import { computed } from '@ember/object';
import { inject } from '@ember/service';
import { reads } from '@ember/object/computed';

export default Component.extend({
  init() {
    this._super(...arguments);
    if (!this.isPaused) {
      this.runTimer();
    }
  },
  willDestroyElement() {
    this._super(...arguments);
    this.stopTimer();
  },
  studyingTimer: inject(),
  countedSeconds: reads('studyingTimer.countedSeconds'),
  isPaused: false,
  timer: null,
  displayValue: computed('countedSeconds', function() {
    const mins = Math.floor(this.countedSeconds / 60);
    const seconds = this.countedSeconds % 60;
    return `${leadingZero(mins)}:${leadingZero(seconds)}`;
  }),
  runTimer() {
    this.set(
      'timer',
      setInterval(() => {
        if (!this.isPaused)
          this.studyingTimer.incrementProperty('countedSeconds');
      }, 1000),
    );
  },
  stopTimer() {
    clearInterval(this.timer);
    this.set('timer', null);
  },
  togglePause() {
    this.set('isPaused', !this.isPaused);
    if (!this.isPaused && !this.timer) this.runTimer();
  },
});

function leadingZero(timeItem) {
  return timeItem.toString().length === 1 ? `0${timeItem}` : timeItem;
}
