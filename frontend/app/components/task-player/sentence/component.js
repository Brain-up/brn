import Component from '@ember/component';
// import { A } from '@ember/array';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
// import shuffleArray from 'brn/utils/shuffle-array';
import customTimeout from 'brn/utils/custom-timeout';
import { action } from '@ember/object';

export default class SentenceComponent extends Component {
  shuffledWords = null;
  lastAnswer = null;
  exerciseResultIsVisible = false;
  taskResultIsVisible = false;
  previousTaskWords = null;

  @service('audio') audio;

  classNames = ['flex-1', 'flex', 'flex-col'];

  attributeBindings = [
    'task.id:data-test-task-id',
    'task.exercise.id:data-test-task-exercise-id',
  ];

  @tracked currentAnswerObject = null;

  get audioFiles() {
    return this.task.answerParts.map(({ audioFileUrl }) => {
      return `/audio/${audioFileUrl}`;
    });
  }

  didReceiveAttrs() {
    if (this.previousTaskWords !== this.task.words) {
      this.shuffle();
      this.set('lastAnswer', null);
    }
    this.set('previousTaskWords', this.task.words);
    this.set('exerciseResultIsVisible', false);
  }

  shuffle() {
    // this.set('shuffledWords', A(shuffleArray(this.task.words)));
    // this.notifyPropertyChange('shuffledWords');
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
        this.task.correctAnswer.mapBy('word'),
      );

      this.isCorrect = isCorrect;

      isCorrect
        ? await this.handleCorrectAnswer()
        : await this.handleWrongAnswer();
    }
  }

  @action
  handleSubmit(word) {
    this.set('lastAnswer', word);
    if (word !== this.task.word) {
      const currentWordsOrder = Array.from(this.shuffledWords);
      this.task.set('nextAttempt', true);
      this.task.set('repetitionCount', this.task.repetitionCount + 1);
      this.set('taskResultIsVisible', true);
      while (deepEqual(currentWordsOrder, this.shuffledWords)) {
        this.shuffle();
      }
    } else {
      this.task.savePassed();
      this.task.set('nextAttempt', false);
    }
  }

  showExerciseResult() {
    this.set('exerciseResultIsVisible', true);
  }

  async runNextTaskTimer() {
    this.element.style.setProperty(
      '--word-picture-url',
      `url(${this.task.pictureFileUrl})`,
    );
    this.onRightAnswer();
    await customTimeout(3000);
    if (this.task.isLastTask) {
      this.showExerciseResult();
      await customTimeout(3000);
    }
    this.afterCompleted();
  }

  async runNextAttemptTimer() {
    await customTimeout(2000);
    this.set('taskResultIsVisible', false);
  }
}
({});
