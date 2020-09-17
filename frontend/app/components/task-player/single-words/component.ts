import Component from '@glimmer/component';
import { A } from '@ember/array';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import shuffleArray from 'brn/utils/shuffle-array';
import customTimeout from 'brn/utils/custom-timeout';
import { task, timeout, Task as TaskGenerator } from 'ember-concurrency';
import { action } from '@ember/object';
import { TIMINGS } from 'brn/utils/audio-api';
import { tracked } from '@glimmer/tracking';
import AudioService from 'brn/services/audio';
import StatsService, { StatEvents } from 'brn/services/stats';

interface ITaskPlayerSingleWordsComponent {
  task: any,
  onShuffled(items: string[]): void;
  onRightAnswer(): void;
  onWrongAnswer(): void;
}

export default class TaskPlayerSingleWordsComponent extends Component<ITaskPlayerSingleWordsComponent> {
  @tracked shuffledWords: null | string[] = null;
  @tracked lastAnswer: null | string = null;
  @tracked exerciseResultIsVisible = false;
  @tracked taskResultIsVisible = false;
  @tracked previousTaskWords = null;

  @service('audio') audio!: AudioService;
  @service('stats') stats!: StatsService;

  get task() {
    return this.args.task;
  }

  @(task(function*(this: TaskPlayerSingleWordsComponent) {
    yield timeout(TIMINGS.SUCCESS_ANSWER_NOTIFICATION)
    this.args.onRightAnswer();
  }).restartable())
  runNextTaskTimer!: TaskGenerator<any, any>

  @(task(function*(this: TaskPlayerSingleWordsComponent) {
    yield customTimeout(2000);
    this.taskResultIsVisible = false;
    this.args.onWrongAnswer();
  }).drop())
  showTaskResult!: TaskGenerator<any, any>

  @action
  updateWords() {
    if (this.previousTaskWords !== this.task.words) {
      this.shuffle();
      this.lastAnswer = null;
    }
    this.previousTaskWords = this.task.words;
    this.exerciseResultIsVisible = false;
  }

  shuffle() {
    this.shuffledWords = A(shuffleArray(this.task.words));
    if (typeof this.args.onShuffled === 'function') {
      this.args.onShuffled(this.shuffledWords);
    }
  }

  @action
  handleSubmit(word: string) {
    this.lastAnswer = word;
    if (word !== this.task.word) {
      this.stats.addEvent(StatEvents.WrongAnswer);
      this.stats.addEvent(StatEvents.Repeat);
      const currentWordsOrder = Array.from(this.shuffledWords || []);
      this.task.set('repetitionCount', this.task.repetitionCount + 1);
      this.task.set('nextAttempt', true);
      this.taskResultIsVisible = true;
      while (deepEqual(currentWordsOrder, this.shuffledWords)) {
        this.shuffle();
      }
    } else {
      this.stats.addEvent(StatEvents.RightAnswer);
      this.task.set('nextAttempt', false);
    }
  }
}

