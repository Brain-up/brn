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
  activeTask = null;

  @tracked mode = ''; // listen, interact, task
  get componentType() {
    return `task-player/${dasherize(this.task.exerciseType)}`;
  }
  get disableAnswers() {
    if (this.mode === MODES.INTERACT) {
      return this.audio.isPlaying;
    }
    return this.audio.isPlaying || this.disableAudioPlayer;
  }
  didReceiveAttrs() {
    if (this.justEnteredTask === false && this._task !== this.task) {
      if (Ember.testing) {
        this.setMode(MODES.TASK);
      } else {
        if (this.taskModelName !== 'task/sentence') {
          this.setMode(MODES.LISTEN);
        }
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

  @action onWrongAnswer({ skipRetry } = { skipRetry: false }) {
    this.taskModeTask.cancelAll();
    if (!skipRetry) {
      this.audio.startPlayTask();
    }
  }

  @action onShuffled(words) {
    // we need this callback, because of singlw-words component shuffle logic
    const sortedWords = this.task.normalizedAnswerOptions.sort((a, b) => {
      return words.indexOf(a.word) - words.indexOf(b.word);
    });
    this.task.set('normalizedAnswerOptions', sortedWords);
  }
  get taskModelName() {
    return this.task.constructor.modelName;
  }
  get orderedPlaylist() {
    const {
      answerOptions,
      selectedItemsOrder,
      normalizedAnswerOptions,
    } = this.task;
    // for ordered tasks we need to align audio stream with object order;
    const modelName = this.task.constructor.modelName;
    if (
      modelName === 'task/single-words' ||
      modelName === 'task/single-simple-words'
    ) {
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

  maybeStartExercise() {
    if (!this.task.get('exercise.isStarted')) {
      this.task.exercise.content.trackTime('start');
    }
  }

  @(task(function*() {
    try {
      this.mode = MODES.TASK;
      yield this.audio.startPlayTask();
    } finally {
      // EOL
    }
  }).keepLatest())
  taskModeTask;

  @(task(function*() {
    try {
      this.mode = MODES.INTERACT;
      while (this.mode === MODES.INTERACT) {
        const playText = this.textToPlay;
        if (playText) {
          this.activeWord = playText;
          this.textToPlay = null;
          let option = this.task.normalizedAnswerOptions.find(
            ({ word }) => word === playText,
          );
          if (option) {
            yield this.audio.setAudioElements([option.audioFileUrl]);
            yield this.audio.playAudio();
          }
        }
        yield timeout(250);
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

  @action async setMode(mode, ...args) {
    if (this.activeTask) {
      try {
        this.activeTask.cancel();
        await this.activeTask;
      } catch (e) {
        // EOL
      }
    }
    this.audio.stop();
    if (mode === MODES.INTERACT) {
      this.activeTask = this.interactModeTask.perform(...args);
    } else if (mode === MODES.TASK) {
      this.activeTask = this.taskModeTask.perform(...args);
    } else if (mode === MODES.LISTEN) {
      this.activeTask = this.listenModeTask.perform(...args);
    }
    return this.activeTask;
  }

  @action
  async startTask() {
    this.justEnteredTask = false;
    this.maybeStartExercise();
    this.studyingTimer.runTimer();
    if (Ember.testing) {
      await this.setMode(MODES.TASK);
    } else {
      await this.setMode(MODES.LISTEN);
    }
    // await this.setMode('interact');
    // await this.setMode('task');
  }
}
