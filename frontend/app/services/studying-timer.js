import Service from '@ember/service';

export default Service.extend({
  countedSeconds: 0,
  isPaused: false,
  setTime(seconds) {
    this.set('countedSeconds', seconds);
  },
  togglePause() {
    this.set('isPaused', !this.isPaused);
  },
  pause() {
    this.set('isPaused', true);
  },
  resume() {
    this.set('isPaused', false);
  },
});
