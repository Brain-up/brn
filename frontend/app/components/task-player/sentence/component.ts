import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';
import { action } from '@ember/object';
import { urlForAudio } from 'brn/utils/file-url';
import { MODES } from 'brn/utils/task-modes';
import { task, Task as TaskGenerator } from 'ember-concurrency';
import StatsService, { StatEvents } from 'brn/services/stats';
import Task from 'brn/models/task';
import AudioService from 'brn/services/audio';

interface ISentenceTaskFields {
  answerParts: any[];
  selectedItemsOrder: any[] & { firstObject: any; lastObject: any };
  isLastTask: boolean;
  savePassed(): void;
  answerOptions: any;
  repetitionCount: number;
  set(key: string, value: number): void;
  parent: {
    tasks: Task[];
  };
}

interface ISentenceComponentArgs {
  task: Task & ISentenceTaskFields;
  mode: keyof typeof MODES;
  disableAnswers: boolean;
  activeWord: string;
  disableAudioPlayer: boolean;
  onPlayText(): void;
  onRightAnswer(): void;
  onWrongAnswer(): void;
}

interface SentenceAnswer {}

export default class SentenceComponent extends Component<ISentenceComponentArgs> {
  @service('stats') stats!: StatsService;
  @tracked exerciseResultIsVisible = false;

  get task() {
    return this.args.task;
  }

  @tracked wrongAnswerParts = [];

  @service('audio') audio!: AudioService;

  @tracked isCorrect = false;

  @tracked currentAnswerObject: Record<string, unknown> | null = null;

  get audioFiles() {
    return (
      this.task?.answerParts.map(({ audioFileUrl }) => {
        return urlForAudio(audioFileUrl);
      }) || []
    );
  }

  get answerCompleted() {
    if (
      !this.currentAnswerObject ||
      Object.keys(this.currentAnswerObject).length <
        (this.task?.answerParts.length || 0)
    ) {
      return false;
    } else {
      return Object.values(this.currentAnswerObject).reduce(
        (isCompleted, currentValue) => {
          isCompleted = isCompleted && !!currentValue;
          return isCompleted;
        },
        true,
      );
    }
  }

  @(task(function* (this: SentenceComponent, selected) {
    this.currentAnswerObject = {
      ...this.currentAnswerObject,
      [selected.wordType]: selected.word,
    };
    if (this.answerCompleted && this.currentAnswerObject !== null) {
      const isCorrect = deepEqual(
        this.task.selectedItemsOrder.map(
          (orderName) => (this.currentAnswerObject as any)[orderName],
        ),
        this.task?.answerParts.mapBy('word') || [],
      );

      this.isCorrect = isCorrect;

      if (
        Object.keys(this.currentAnswerObject).length ===
        (this.task?.answerParts.length || 0)
      ) {
        if (isCorrect) {
          yield this.handleCorrectAnswer();
        } else {
          yield this.handleWrongAnswer();
        }
      }
    }
  }).drop())
  showTaskResult!: TaskGenerator<any, any>;

  @action resetAnswerObject() {
    this.currentAnswerObject = null;
    if (this.args.mode === MODES.TASK) {
      this.audio.startPlayTask(this.audioFiles);
    }
  }

  @action
  async checkMaybe(selectedData: SentenceAnswer) {
    this.showTaskResult.perform(selectedData);
  }

  showExerciseResult() {
    this.exerciseResultIsVisible = true;
  }

  async runNextTaskTimer() {
    await customTimeout(3000);
    if (this.task.isLastTask) {
      this.showExerciseResult();
      await customTimeout(3000);
    }
    this.args.onRightAnswer();
  }

  async handleWrongAnswer() {
    this.stats.addEvent(StatEvents.WrongAnswer);
    await customTimeout(1000);
    this.task.set('repetitionCount', this.task.repetitionCount + 1);
    this.currentAnswerObject = null;
    this.args.onWrongAnswer();
  }

  async handleCorrectAnswer() {
    this.stats.addEvent(StatEvents.RightAnswer);
    this.task.savePassed();
    await this.runNextTaskTimer();
  }
}
