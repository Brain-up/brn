import Component from '@ember/component';
import { computed } from '@ember/object';
import { dasherize } from '@ember/string';
import { or, not } from 'ember-awesome-macros';
import { inject as service } from '@ember/service';

export default Component.extend({
  init() {
    this._super(...arguments);
    this.set('justEnteredTask', true);
  },
  audio: service(),
  studyingTimer: service(),
  task: null,
  componentType: computed('task.exerciseType', function() {
    return `task-player/${dasherize(this.task.exerciseType)}`;
  }),
  disableAnswers: computed('audio.isPlaying', 'disableAudioPlayer', function() {
    return this.audio.isPlaying || this.disableAudioPlayer;
  }),
  disableAudioPlayer: or('task.pauseExecution', not('studyingTimer.isStarted')),
  onRightAnswer() {},
  afterCompleted() {},
  async startTask() {
    this.studyingTimer.runTimer();
    this.task.exercise.content.trackTime('start');
    this.set('justEnteredTask', false);
  },
});
