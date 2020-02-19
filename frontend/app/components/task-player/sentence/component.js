import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';

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
    return this.task.answerParts.map(({ audioFileUrl }) => {
      return `/audio/${audioFileUrl}`;
    });
  }

  get answerCompleted() {
    if (
      !this.currentAnswerObject ||
      Object.keys(this.currentAnswerObject).length <
        this.task.answerParts.length
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

  async checkMaybe(selectedData) {
    this.currentAnswerObject = {
      ...this.currentAnswerObject,
      [selectedData.wordType]: selectedData.word,
    };
    if (this.answerCompleted) {
      const isCorrect = deepEqual(
        this.task.selectedItemsOrder.map(
          (orderName) => this.currentAnswerObject[orderName],
        ),
        this.task.answerParts.mapBy('word'),
      );

      this.isCorrect = isCorrect;

      if (
        Object.keys(this.currentAnswerObject).length ===
        this.task.answerParts.length
      ) {
        isCorrect
          ? await this.handleCorrectAnswer()
          : await this.handleWrongAnswer();
      }
    }
  }

  showExerciseResult() {
    this.exerciseResultIsVisible = true;
  }

  async runNextTaskTimer() {
    this.args.onRightAnswer();
    await customTimeout(3000);
    if (this.task.isLastTask) {
      this.showExerciseResult();
      await customTimeout(3000);
    }
  }

  async handleWrongAnswer() {
    await customTimeout(1000);
    this.task.set('repetitionCount', this.task.repetitionCount + 1);
    this.audio.player.playAudio();
    this.currentAnswerObject = null;
  }

  async handleCorrectAnswer() {
    this.task.savePassed();
    this.runNextTaskTimer();
  }
}
