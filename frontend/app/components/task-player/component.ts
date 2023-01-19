/* eslint-disable ember/no-component-lifecycle-hooks */
// eslint-disable-next-line ember/no-classic-components
import Component from '@ember/component';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { timeout, task, TaskInstance } from 'ember-concurrency';
import { MODES } from 'brn/utils/task-modes';
import Ember from 'ember';
import StatsService, { StatEvents } from 'brn/services/stats';
import AudioService from 'brn/services/audio';
import StudyingTimerService from 'brn/services/studying-timer';
import TaskModel from 'brn/models/task';
import type AnswerOption from 'brn/utils/answer-option';
import { ExerciseMechanism } from 'brn/serializers/application';

export default class TaskPlayerComponent extends Component {
  @service('audio') audio!: AudioService;
  @service('stats') stats!: StatsService;
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
    super.willDestroyElement(...arguments);
    this.audio.stopNoise();
  }

  @tracked mode = ''; // listen, interact, task
  get componentType() {
    const mechanism = this.task.exerciseMechanism;
    let postfix = '';

    if (mechanism === ExerciseMechanism.SIGNALS) {
      postfix = 'signal';
    } else if (mechanism === ExerciseMechanism.MATRIX) {
      postfix = 'words-sequences';
    } else if (mechanism === ExerciseMechanism.WORDS) {
      postfix = 'single-simple-words';
    } else {
      throw new Error('Unknown mechanism: ' + mechanism);
    }

    return `task-player/${postfix}`;
  }
  get disableAnswers() {
    if (this.mode === MODES.INTERACT) {
      return this.audio.isPlaying;
    }
    return this.audio.isPlaying || this.disableAudioPlayer;
  }
  didReceiveAttrs() {
    super.didReceiveAttrs();
    if (this.justEnteredTask === false && this._task !== this.task) {
      if (Ember.testing) {
        this.setMode(MODES.TASK);
      } else {
        if (
          this.taskModelName !== ExerciseMechanism.MATRIX &&
          this.taskModelName !== ExerciseMechanism.SIGNALS
        ) {
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

  @action async onWrongAnswer({ skipRetry } = { skipRetry: false }) {
    await this.taskModeTask.cancelAll();
    if (!skipRetry) {
      this.audio.startPlayTask();
    }
  }

  @action onShuffled(words: string[]) {
    // we need this callback, because of singlw-words component shuffle logic
    const sortedWords = this.task.normalizedAnswerOptions.sort((a, b) => {
      return words.indexOf(a.word) - words.indexOf(b.word);
    });
    this.task.set('normalizedAnswerOptions', sortedWords);
  }
  get taskModelName() {
    return this.task.exerciseMechanism;
  }
  get orderedPlaylist(): AnswerOption[] {
    const { answerOptions, selectedItemsOrder, normalizedAnswerOptions } =
      this.task;
    // for ordered tasks we need to align audio stream with object order;
    

    if (this.task.exerciseMechanism === ExerciseMechanism.SIGNALS) {
      return answerOptions;
    }
    if (this.task.exerciseMechanism === ExerciseMechanism.WORDS) {
      return normalizedAnswerOptions;
    }

    if (this.task.exerciseMechanism === ExerciseMechanism.MATRIX) {
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

    throw new Error(`Unknown task type - ${this.task.exerciseMechanism}`);
  }

  @(task(function* (this: TaskPlayerComponent) {
    try {
      this.mode = MODES.LISTEN;
      for (const option of this.orderedPlaylist) {
        this.activeWord = option.word;
        // tone object case
        if (typeof option.audioFileUrl === 'object' && option.audioFileUrl !== null) {
          yield this.audio.setAudioElements([option.audioFileUrl]);
        } else {
          const useGeneratedUrl =
            option.audioFileUrl && this.task.usePreGeneratedAudio;
          yield this.audio.setAudioElements([
            useGeneratedUrl
              ? option.audioFileUrl!
              : this.audio.audioUrlForText(option.wordPronounce ?? option.word),
          ]);
        }

        yield this.audio.playAudio();
        yield timeout(1500);
        this.activeWord = null;
      }
    } catch (e) {
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

  @(task(function* (this: TaskPlayerComponent) {
    try {
      this.mode = MODES.TASK;
      yield this.audio.startPlayTask();
    } catch (e) {
      // EOL
    } finally {
      // EOL
    }
  }).keepLatest())
  taskModeTask!: any;

  @(task(function* (this: TaskPlayerComponent) {
    try {
      this.mode = MODES.INTERACT;
      while (this.mode === MODES.INTERACT) {
        const playText = this.textToPlay;
        if (playText) {
          this.activeWord = playText;
          this.textToPlay = null;
          const option = this.task.normalizedAnswerOptions.find(
            ({ word }: any) => word === playText,
          );
          if (option) {

            if (typeof option.audioFileUrl === 'object' && option.audioFileUrl !== null) {
              yield this.audio.setAudioElements([option.audioFileUrl]);
            } else {
              const useGeneratedUrl =
              option.audioFileUrl && this.task.usePreGeneratedAudio;

              yield this.audio.setAudioElements([
                useGeneratedUrl
                  ? (option.audioFileUrl as string)
                  : this.audio.audioUrlForText(option.wordPronounce),
              ]);
            }
            
            yield this.audio.playAudio();
          }
        }
        yield timeout(250);
        this.activeWord = null;
      }
    } catch (e) {
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
          } catch (e) {
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
      (this.activeTask as any).catch(() => {
        // EOL
      });
      return this.activeTask;
    } catch (e) {
      // EOLS
    }
  }

  @action async preloadNoise() {
    await this.audio.preloadNoiseAudio();
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
      } catch (e) {
        // EOL
      } finally {
        try {
          await this.setMode(MODES.INTERACT);
        } catch (e) {
          // EOL
        }
      }
    }
  }
}
