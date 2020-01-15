import Service from '@ember/service';
import { reads } from '@ember/object/computed';

export default Service.extend({
  countedSeconds: 0,
  isPaused: false,
  isStarted: reads('timerInstance.isStarted'),
  register(timer) {
    this.set('timerInstance', timer);
  },
  runTimer() {
    this.set('isPaused', false);
    return this.timerInstance.togglePause();
  },
  setTime(seconds) {
    this.set('countedSeconds', seconds);
  },
  togglePause() {
    this.set('isPaused', !this.isPaused);
  },
  pause() {
    this.set('isPaused', true);
  },
});
