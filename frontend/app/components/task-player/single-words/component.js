import Component from '@ember/component';
import { A } from '@ember/array';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import shuffleArray from 'brn/utils/shuffle-array';
import customTimeout from 'brn/utils/custom-timeout';
import { task } from 'ember-concurrency';

export default class TaskPlayerComponent extends Component {
  shuffledWords = null;
  lastAnswer = null;
  exerciseResultIsVisible = false;
  taskResultIsVisible = false;
  previousTaskWords = null;

  willDestroy() {
    super.willDestroy(...arguments);
    this.nextTaskTimer.cancelAll();
    this.nextAttemptTimer.cancelAll();
  }

  @service('audio') audio;

  @task(function*() {
    this.element.style.setProperty(
      '--word-picture-url',
      `url(${this.task.pictureFileUrl})`,
    );
    this.onRightAnswer();
    yield customTimeout(3000);
    if (this.task.isLastTask) {
      this.showExerciseResult();
      yield customTimeout(3000);
    }
    this.afterCompleted();
  })
  nextTaskTimer;

  @task(function*() {
    yield customTimeout(2000);
    this.set('taskResultIsVisible', false);
  })
  nextAttemptTimer;

  classNames = ['flex-1', 'flex', 'flex-col'];

  attributeBindings = [
    'task.id:data-test-task-id',
    'task.exercise.id:data-test-task-exercise-id',
  ];

  didReceiveAttrs() {
    if (this.previousTaskWords !== this.task.words) {
      this.shuffle();
      this.set('lastAnswer', null);
    }
    this.set('previousTaskWords', this.task.words);
    this.set('exerciseResultIsVisible', false);
  }

  shuffle() {
    this.set('shuffledWords', A(shuffleArray(this.task.words)));
    this.notifyPropertyChange('shuffledWords');
  }

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
}
({});
