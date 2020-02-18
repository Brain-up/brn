import Component from '@ember/component';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';

export default class SentenceComponent extends Component {
  @tracked exerciseResultIsVisible = false;

  @tracked taskCopy;

  @tracked wrongAnswerParts = [];

  @service('audio') audio;

  classNames = ['flex-1', 'flex', 'flex-col'];

  attributeBindings = [
    'task.id:data-test-task-id',
    'task.exercise.id:data-test-task-exercise-id',
  ];

  @tracked isCorrect = false;

  @tracked currentAnswerObject = null;

  didReceiveAttrs() {
    this.set('exerciseResultIsVisible', false);
  }

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
    this.set('exerciseResultIsVisible', true);
  }

  async runNextTaskTimer() {
    this.onRightAnswer();
    await customTimeout(3000);
    if (this.task.isLastTask) {
      this.showExerciseResult();
      await customTimeout(3000);
    }
    this.afterCompleted();
  }

  async handleWrongAnswer() {
    await customTimeout(1000);
    this.task.set('repetitionCount', this.task.repetitionCount + 1);
    this.notifyPropertyChange('audioFiles');
    this.currentAnswerObject = null;
  }

  async handleCorrectAnswer() {
    this.task.savePassed();
    this.runNextTaskTimer();
  }
}
({});
