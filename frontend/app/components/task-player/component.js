import Component from '@ember/component';
import { dasherize } from '@ember/string';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { timeout, task } from 'ember-concurrency';
import { MODES } from 'brn/utils/task-modes';
import Ember from 'ember';
export default class TaskPlayerComponent extends Component {
  @service
  audio;
  @service
  studyingTimer;
  @tracked
  justEnteredTask = true;
  @tracked
  task = null;
  @tracked
  activeWord = null;
  @tracked
  textToPlay = null;
  tagName = '';
  @tracked mode = ''; // listen, interact, task
  get componentType() {
    return `task-player/${dasherize(this.task.exerciseType)}`;
  }
  get disableAnswers() {
    if (this.mode === MODES.INTERACT) {
      return false;
    }
    return this.audio.isPlaying || this.disableAudioPlayer;
  }
  didReceiveAttrs() {
    if (this.justEnteredTask === false && this._task !== this.task) {
      if (Ember.testing) {
        this.setMode(MODES.TASK);
      } else {
        this.setMode(MODES.LISTEN);
      }
    }
  }

  get disableAudioPlayer() {
    return (
      this.task.pauseExecution ||
      !this.studyingTimer.isStarted ||
      this.justEnteredTask
    );
  }

  // @action
  onRightAnswer() {
    // EOL
  }

  @action onWrongAnswer() {
    this.taskModeTask.cancelAll();
    this.audio.startPlayTask();
  }

  @action onShuffled(words) {
    // we need this callback, because of singlw-words component shuffle logic
    const sortedWords = this.task.normalizedAnswerOptions.sort((a, b) => {
      return words.indexOf(a.word) - words.indexOf(b.word);
    });
    this.task.set('normalizedAnswerOptions', sortedWords);
  }

  get orderedPlaylist() {
    const {
      answerOptions,
      selectedItemsOrder,
      normalizedAnswerOptions,
    } = this.task;
    // for ordered tasks we need to align audio stream with object order;
    const modelName = this.task.constructor.modelName;
    if (modelName === 'task/single-words') {
      return normalizedAnswerOptions;
    }
    if (modelName === 'task/words-sequences' || modelName === 'task/sentence') {
      const sortedItems = [];
      const length = answerOptions[selectedItemsOrder[0]].length;
      for (let i = 0; i < length; i++) {
        selectedItemsOrder.forEach((key) => {
          sortedItems.push(
            this.task.normalizedAnswerOptions.find(
              ({ word }) => word === answerOptions[key][i].word,
            ),
          );
        });
      }
      return sortedItems;
    }
    throw new Error(`Unknown task type - ${modelName}`);
  }

  @(task(function*() {
    try {
      this.interactModeTask.cancelAll();
      this.taskModeTask.cancelAll();
      this.mode = MODES.LISTEN;
      for (let option of this.orderedPlaylist) {
        this.activeWord = option.word;
        yield this.audio.setAudioElements([option.audioFileUrl]);
        yield this.audio.playAudio();
        yield timeout(1500);
        this.activeWord = null;
      }
    } finally {
      this.activeWord = null;
      this.audio.stop();
    }
  }).keepLatest())
  listenModeTask;

  @(task(function*() {
    try {
      this.interactModeTask.cancelAll();
      this.listenModeTask.cancelAll();
      this.mode = MODES.TASK;
      yield this.audio.startPlayTask();
      this.studyingTimer.runTimer();
      if (!this.task.get('exercise.isStarted')) {
        this.task.exercise.content.trackTime('start');
      }
    } finally {
      // EOL
    }
  }).keepLatest())
  taskModeTask;

  @(task(function*() {
    try {
      this.taskModeTask.cancelAll();
      this.listenModeTask.cancelAll();
      this.mode = MODES.INTERACT;
      while (this.mode === MODES.INTERACT) {
        if (this.textToPlay) {
          this.activeWord = this.textToPlay;
          let option = this.task.normalizedAnswerOptions.find(
            ({ word }) => word === this.textToPlay,
          );
          if (option) {
            yield this.audio.setAudioElements([option.audioFileUrl]);
            yield this.audio.playAudio();
          }
        }
        yield timeout(1500);
        this.activeWord = null;
      }
    } finally {
      this.audio.stop();
      this.activeWord = null;
      this.textToPlay = null;
    }
  }).keepLatest())
  interactModeTask;

  @action playText(text) {
    this.textToPlay = text;
  }

  @action
  onModeChange(mode) {
    this.setMode(mode);
  }

  @action setMode(mode, ...args) {
    if (mode === MODES.INTERACT) {
      return this.interactModeTask.perform(...args);
    } else if (mode === MODES.TASK) {
      return this.taskModeTask.perform(...args);
    } else if (mode === MODES.LISTEN) {
      return this.listenModeTask.perform(...args);
    }
  }

  @action
  async startTask() {
    this.justEnteredTask = false;
    if (Ember.testing) {
      await this.setMode('task');
    } else {
      await this.setMode(MODES.LISTEN);
    }
    // await this.setMode('interact');
    // await this.setMode('task');
  }
}
