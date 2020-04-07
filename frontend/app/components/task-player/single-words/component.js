import Component from '@ember/component';
import { A } from '@ember/array';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import shuffleArray from 'brn/utils/shuffle-array';
import customTimeout from 'brn/utils/custom-timeout';
import { task, timeout } from 'ember-concurrency';
import { action } from '@ember/object';
import { TIMINGS } from 'brn/utils/audio-api';
import { tracked } from '@glimmer/tracking';


export default class TaskPlayerComponent extends Component {
  @tracked shuffledWords = null;
  @tracked lastAnswer = null;
  @tracked exerciseResultIsVisible = false;
  @tracked taskResultIsVisible = false;
  @tracked previousTaskWords = null;

  @service('audio') audio;

  @(task(function*() {
    yield timeout(TIMINGS.SUCCESS_ANSWER_NOTIFICATION)
    this.onRightAnswer();
  }).restartable())
  runNextTaskTimer;

  @(task(function*() {
    yield customTimeout(2000);
    this.taskResultIsVisible = false;
    this.onWrongAnswer();
  }).drop())
  showTaskResult;

  classNames = ['flex-1', 'flex', 'flex-col'];

  attributeBindings = [
    'task.id:data-test-task-id',
    'task.exercise.id:data-test-task-exercise-id',
  ];

  didReceiveAttrs() {
    if (this.previousTaskWords !== this.task.words) {
      this.shuffle();
      this.lastAnswer = null;
    }
    this.previousTaskWords = this.task.words;
    this.exerciseResultIsVisible = false;
  }

  shuffle() {
    this.shuffledWords = A(shuffleArray(this.task.words));
  }

  @action
  handleSubmit(word) {
    this.lastAnswer = word;
    if (word !== this.task.word) {
      const currentWordsOrder = Array.from(this.shuffledWords);
      this.task.set('repetitionCount', this.task.repetitionCount + 1);
      this.task.set('nextAttempt', true);
      this.taskResultIsVisible = true;
      while (deepEqual(currentWordsOrder, this.shuffledWords)) {
        this.shuffle();
      }
    } else {
      this.task.set('nextAttempt', false);
    }
  }
}

