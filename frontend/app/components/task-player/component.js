import Component from '@ember/component';
import { A } from '@ember/array';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import { timeout } from 'ember-concurrency';

export default class TaskPlayerComponent extends Component {
  shuffledWords = null;
  lastAnswer = null;
  exerciseResultIsVisible = false;
  taskResultIsVisible = false;

  @service('audio') audio;

  classNames = ['flex-1', 'flex', 'flex-col'];

  attributeBindings = [
    'task.id:data-test-task-id',
    'task.exercise.id:data-test-task-exercise-id',
  ];

  didReceiveAttrs() {
    this.shuffle();
    this.set('lastAnswer', null);
    this.set('exerciseResultIsVisible', false);
  }

  shuffle() {
    this.set('shuffledWords', A(shuffleArray(this.task.words)));
    this.notifyPropertyChange('shuffledWords');
  }

  onRightAnswer() {}

  handleSubmit(word) {
    this.set('lastAnswer', word);
    if (word !== this.task.word) {
      const currentWordsOrder = Array.from(this.shuffledWords);
      this.task.set('nextAttempt', true);
      this.set('taskResultIsVisible', true);
      while (deepEqual(currentWordsOrder, this.shuffledWords)) {
        this.shuffle();
      }
      this.audio.player.playAudio();
    } else {
      this.task.savePassed();
      this.task.set('nextAttempt', false);
    }
  }

  showExerciseResult() {
    this.set('exerciseResultIsVisible', true);
  }

  async runNextTaskTimer() {
    await timeout(3000);
    if (this.task.isLastExerciseTask) {
      this.showExerciseResult();
      await timeout(3000);
    }
    this.onRightAnswer();
  }

  async runNextAttemptTimer() {
    await timeout(2000);
    this.set('taskResultIsVisible', false);
  }
}
({});

function shuffleArray(a) {
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}
