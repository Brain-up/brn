import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';
import { action } from '@ember/object';
import { urlForAudio } from 'brn/utils/file-url';
import { MODES } from 'brn/utils/task-modes';
import { task } from 'ember-concurrency';

export default class SentenceComponent extends Component {
  @tracked exerciseResultIsVisible = false;

  get task() {
    return this.args.task;
  }

  @tracked wrongAnswerParts = [];

  @service('audio') audio;

  @tracked isCorrect = false;

  @tracked currentAnswerObject = null;

  get audioFiles() {
    return this.task?.answerParts.map(({ audioFileUrl }) => {
      return urlForAudio(audioFileUrl);
    }) || [];
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

  @(task(function*(selected) {
    this.currentAnswerObject = {
      ...this.currentAnswerObject,
      [selected.wordType]: selected.word,
    };
    if (this.answerCompleted) {
      const isCorrect = deepEqual(
        this.task.selectedItemsOrder.map(
          (orderName) => this.currentAnswerObject[orderName],
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
  showTaskResult;

  @action resetAnswerObject() {
    this.currentAnswerObject = null;
    if (this.args.mode === MODES.TASK) {
      this.audio.startPlayTask(this.audioFiles);
    }
  }

  @action
  async checkMaybe(selectedData) {
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
    await customTimeout(1000);
    this.task.set('repetitionCount', this.task.repetitionCount + 1);
    this.currentAnswerObject = null;
    this.args.onWrongAnswer();
  }

  async handleCorrectAnswer() {
    this.task.savePassed();
    await this.runNextTaskTimer();
  }
}
