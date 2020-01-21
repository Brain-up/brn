import Service from '@ember/service';
import { reads } from '@ember/object/computed';
import config from 'brn/config/environment';

export default Service.extend({
  willDestroy() {
    this._super(...arguments);
    this.idleWatcher && this.idleWatcher.stop();
  },
  countedSeconds: 0,
  isPaused: false,
  isStarted: reads('timerInstance.isStarted'),
  register(timer) {
    this.set('timerInstance', timer);
    this.startIdleWatcher();
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
  startIdleWatcher() {
    const player = this;
    /* eslint-disable no-undef */
    this.set(
      'idleWatcher',
      new IdleJs({
        idle: player.timerInstance.idleTime || config.idleTime,
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
});
