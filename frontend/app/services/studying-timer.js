import Service from '@ember/service';
import { action } from '@ember/object';
import config from 'brn/config/environment';
import { tracked } from '@glimmer/tracking';

export default class StudyingTimerService extends Service {
  willDestroy() {
    super.willDestroy();
    this.idleWatcher && this.idleWatcher.stop();
  }
  @tracked
  idleWatcher = null;
  @tracked
  countedSeconds = 0;
  @tracked
  isPaused = false;
  @tracked
  timerInstance = null;
  get isStarted() {
    return this.timerInstance && this.timerInstance.isStarted;
  }
  @action
  register(timer) {
    this.countedSeconds = 0;
    this.timerInstance = timer;
    this.startIdleWatcher();
  }
  @action
  runTimer() {
    this.resume();
    return this.timerInstance.runTimer();
  }
  @action
  addTime(seconds) {
    this.countedSeconds += seconds;
  }
  @action
  togglePause() {
    this.isPaused = !this.isPaused;
  }
  @action
  pause() {
    this.isPaused = true;
  }
  @action
  resume() {
    this.isPaused = false;
  }
  @action
  startIdleWatcher() {
    const player = this;
    const { timerInstance } = player;
    /* eslint-disable no-undef */
    this.idleWatcher = new IdleJs({
      idle: timerInstance.idleTimeout || config.idleTimeout,
      onIdle() {
        player.pause();
      },
      onActive() {
        timerInstance.relaunchStartedTimer();
      },
      onHide() {
        player.pause();
      },
      onShow() {
        timerInstance.relaunchStartedTimer();
      },
    });
    this.idleWatcher.start();
  }
}
