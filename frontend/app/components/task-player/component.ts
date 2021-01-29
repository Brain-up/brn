import Component from '@ember/component';
import { dasherize } from '@ember/string';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { timeout, task, TaskInstance  } from 'ember-concurrency';
import { MODES } from 'brn/utils/task-modes';
import Ember from 'ember';
import StatsService, { StatEvents } from 'brn/services/stats';
import AudioService from 'brn/services/audio';
import StudyingTimerService from 'brn/services/studying-timer';
import TaskModel from 'brn/models/task';

export default class TaskPlayerComponent extends Component {
  @service('audio') audio!: AudioService;
  @service("stats")  stats!: StatsService;
  @service('studying-timer') studyingTimer!: StudyingTimerService;
  @tracked justEnteredTask = true;
  @tracked task!: TaskModel;
  _task = undefined;
  @tracked
  activeWord: string | null = null;
  @tracked
  textToPlay: string | null = null;
  tagName = '';
  activeTask: null | TaskInstance<any> = null;
  willDestroyElement() {
    this.audio.stopNoise();
  }

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
        if (this.taskModelName !== 'task/sentence' && this.taskModelName !== 'task/signal') {
          this.setMode(MODES.LISTEN);
        }
      }
    }
  }

  get disableAudioPlayer(): boolean {
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

  @action onShuffled(words: any) {
    // we need this callback, because of singlw-words component shuffle logic
    const sortedWords = this.task.normalizedAnswerOptions.sort((a: any, b: any) => {
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
    if (modelName === 'task/signal') {
      return answerOptions;
    }
    if (
      modelName === 'task/single-words' ||
      modelName === 'task/single-simple-words' ||
      modelName === 'task/phrase'
    ) {
      return normalizedAnswerOptions;
    }
    if (modelName === 'task/words-sequences' || modelName === 'task/sentence') {
      const sortedItems: any[] = [];
      const length = answerOptions[selectedItemsOrder[0]].length;
      for (let i = 0; i < length; i++) {
        selectedItemsOrder.forEach((key: string) => {
          sortedItems.push(
            this.task.normalizedAnswerOptions.find(
              ({ word }: any) => word === answerOptions[key][i].word,
            ),
          );
        });
      }
      return sortedItems;
    }
    throw new Error(`Unknown task type - ${modelName}`);
  }

  @(task(function*(this: TaskPlayerComponent) {
    try {
      this.mode = MODES.LISTEN;
      for (let option of this.orderedPlaylist) {
        this.activeWord = option.word;
        yield this.audio.setAudioElements([option.audioFileUrl]);
        yield this.audio.playAudio();
        yield timeout(1500);
        this.activeWord = null;
      }
    } catch(e) {
      // EOL
    } finally {
      this.activeWord = null;
      this.audio.stop();
    }
  }).keepLatest())
  listenModeTask!: any;

  maybeStartExercise() {
    if (!this.task.get('exercise.isStarted')) {
      this.stats.addEvent(StatEvents.Start);
      this.task.exercise.trackTime('start');
    }
    this.audio.startNoise();
  }

  @(task(function*(this: TaskPlayerComponent) {
    try {
      this.mode = MODES.TASK;
      yield this.audio.startPlayTask();
    } catch(e) {
      // EOL
    } finally {
      // EOL
    }
  }).keepLatest())
  taskModeTask!: any;

  @(task(function*(this: TaskPlayerComponent) {
    try {
      this.mode = MODES.INTERACT;
      while (this.mode === MODES.INTERACT) {
        const playText = this.textToPlay;
        if (playText) {
          this.activeWord = playText;
          this.textToPlay = null;
          let option = this.task.normalizedAnswerOptions.find(
            ({ word } : any) => word === playText,
          );
          if (option) {
            yield this.audio.setAudioElements([option.audioFileUrl]);
            yield this.audio.playAudio();
          }
        }
        yield timeout(250);
        this.activeWord = null;
      }
    } catch(e) {
      // EOL
    } finally {
      this.audio.stop();
      this.activeWord = null;
      this.textToPlay = null;
    }
  }).keepLatest())
  interactModeTask!: any;

  get isProgressBarVisible() {
    return this.mode === 'task';
  }

  @action playText(text: string) {
    this.textToPlay = text;
  }

  @action
  onModeChange(mode: any) {
    this.setMode(mode);
  }

  @action async setMode(mode: string, ...args: any) {
    try {
      if (this.activeTask) {
        try {
          this.activeTask.cancel();
        } catch (e) {
          // EOL
        } finally {
          try {
            await this.activeTask;
          } catch(e) {
            // EOL
          }
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
      (this.activeTask as any).catch(()=>{
        // EOL
      });
      return this.activeTask;
    } catch(e) {
      // EOLS
    }
  }

  @action
  async startTask() {
    this.justEnteredTask = false;
    this.maybeStartExercise();
    this.studyingTimer.runTimer();
    if (Ember.testing) {
      await this.setMode(MODES.TASK);
    } else {
      try {
        await this.setMode(MODES.LISTEN);
        // Let's switch to interact right after listen if not stopped
      } catch(e) {
        // EOL
      } finally {
        try {
          await this.setMode(MODES.INTERACT);
        } catch(e) {
          // EOL
        }
      }
    }
  }
}
