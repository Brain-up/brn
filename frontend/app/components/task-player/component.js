import Component from '@ember/component';
import { dasherize } from '@ember/string';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';

export default class TaskPlayerComponent extends Component {
  @service
  audio;
  @service
  studyingTimer;
  @tracked
  justEnteredTask = true;
  @tracked
  task = null;
  get componentType() {
    return `task-player/${dasherize(this.task.exerciseType)}`;
  }
  get disableAnswers() {
    return this.audio.isPlaying || this.disableAudioPlayer;
  }

  get disableAudioPlayer() {
    return (
      this.task.pauseExecution ||
      !this.studyingTimer.isStarted ||
      this.justEnteredTask
    );
  }
  onRightAnswer() {}
  async startTask() {
    this.studyingTimer.runTimer();
    this.task.exercise.content.trackTime('start');
    this.set('justEnteredTask', false);
  }
}
