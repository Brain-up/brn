import Service from '@ember/service';
import { reads } from '@ember/object/computed';

export default Service.extend({
  init() {
    this._super(...arguments);
    const player = this;
    /* eslint-disable no-undef */
    this.set(
      'idleWatcher',
      new IdleJs({
        idle: player.idleTime || 10000,
        onIdle: function() {
          player.pause();
        },
        onActive: function() {
          player.timerInstance.runTimer();
        },
        onHide: function() {
          player.pause();
        },
        onShow: function() {
          player.timerInstance.runTimer();
        },
      }),
    );
    this.idleWatcher.start();
  },
  willDestroy() {
    this._super(...arguments);
    this.idleWatcher.stop();
  },
  countedSeconds: 0,
  idleTime: null,
  isPaused: false,
  isStarted: reads('timerInstance.isStarted'),
  register(timer) {
    this.set('timerInstance', timer);
  },
  runTimer() {
    return this.timerInstance.runTimer();
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
  resume() {
    this.set('isPaused', false);
  },
});
