import Component from '@glimmer/component';
import { set, action } from '@ember/object';
import { inject as service } from '@ember/service';
import deepCopy from 'brn/utils/deep-copy';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';
import { TaskItem } from 'brn/utils/task-item';
import { tracked } from '@glimmer/tracking';
import { MODES } from 'brn/utils/task-modes';
import { task, Task as TaskGenerator } from 'ember-concurrency';
import AudioService from 'brn/services/audio';
import StatsService, { StatEvents } from 'brn/services/stats';

function getEmptyTemplate(selectedItemsOrder = []): any {
  return selectedItemsOrder.reduce((result, currentKey) => {
    (result as any)[currentKey] = null;
    return result;
  }, {});
}

interface IWordsSequencesComponentArgs {
  task: any;
  mode: keyof typeof MODES;
  disableAnswers: boolean;
  activeWord: string;
  disableAudioPlayer: boolean;
  onPlayText(): void;
  onRightAnswer(): void;
  onWrongAnswer(params?: { skipRetry: true }): void;
}

export default class WordsSequencesComponent extends Component<IWordsSequencesComponentArgs> {
  @action onInsert() {
    this.updateLocalTasks();
    this.startTask();
  }
  @service audio!: AudioService;
  @service stats!: StatsService;
  @tracked tasksCopy = [];
  @tracked currentAnswerObject: null | Record<string, string> = null;
  @tracked isCorrect = false;
  get task() {
    return this.args.task;
  }
  get mode() {
    return this.args.mode;
  }
  get onWrongAnswer() {
    return this.args.onWrongAnswer;
  }
  get onRightAnswer() {
    return this.args.onRightAnswer;
  }
  get uncompletedTasks() {
    return this.tasksCopy.filter(
      ({ completedInCurrentCycle }) => completedInCurrentCycle === false,
    );
  }
  get firstUncompletedTask(): any {
    return this.uncompletedTasks.firstObject;
  }
  get audioFiles() {
    if (!this.firstUncompletedTask) {
      return [];
    }

    const text = this.firstUncompletedTask.answer
      .map(({ word }) => word)
      .join(' ');
    return [this.audio.audioUrlForText(text)];
  }
  get answerCompleted() {
    return Object.values(this.currentAnswerObject as any).reduce(
      (isCompleted, currentValue) => {
        isCompleted = isCompleted && !!currentValue;
        return isCompleted;
      },
      true,
    );
  }
  startNewTask() {
    this.markCompleted(this.firstUncompletedTask);
    this.startTask();
  }
  markCompleted(task: any) {
    set(task, 'completedInCurrentCycle', true);
    set(task, 'nextAttempt', false);
  }
  markNextAttempt(task: any) {
    set(task, 'nextAttempt', true);
  }
  startTask() {
    this.isCorrect = false;
    this.currentAnswerObject = getEmptyTemplate(this.task.selectedItemsOrder);
    if (this.mode === MODES.TASK) {
      this.audio.startPlayTask(this.audioFiles);
    }
  }
  updateLocalTasks() {
    const completedOrders = this.tasksCopy
      .filterBy('completedInCurrentCycle', true)
      .mapBy('order');
    const tasksCopy = deepCopy(this.task.tasksToSolve).map(
      (copy: { order: number }) => {
        const completedInCurrentCycle = completedOrders.includes(copy.order);
        const copyEquivalent = this.tasksCopy.findBy(
          'order',
          copy.order,
        ) as any;
        return new TaskItem({
          ...copy,
          completedInCurrentCycle,
          nextAttempt: copyEquivalent && !!copyEquivalent.nextAttempt,
          canInteract: true,
        });
      },
    );
    this.tasksCopy = tasksCopy;
  }

  @(task(function* (
    this: WordsSequencesComponent,
    selected: { wordType: string; word: string },
  ) {
    this.currentAnswerObject = {
      ...(this.currentAnswerObject || {}),
      [selected.wordType]: selected.word,
    };
    if (this.answerCompleted) {
      const isCorrect = deepEqual(
        this.task.selectedItemsOrder.map(
          (orderName: string) =>
            (this.currentAnswerObject as any)[orderName] as string,
        ),
        this.firstUncompletedTask.answer.mapBy('word'),
      );

      this.isCorrect = isCorrect;

      if (isCorrect) {
        this.stats.addEvent(StatEvents.RightAnswer);
        yield this.handleCorrectAnswer();
      } else {
        this.stats.addEvent(StatEvents.WrongAnswer);
        yield this.handleWrongAnswer();
      }
    }
  }).drop())
  showTaskResult!: TaskGenerator<any, any>;

  @action
  async checkMaybe(selectedData: any) {
    this.showTaskResult.perform(selectedData);
  }

  async handleWrongAnswer() {
    this.task.wrongAnswers.pushObject(this.firstUncompletedTask.serialize());
    this.markNextAttempt(this.firstUncompletedTask);
    this.updateLocalTasks();
    await customTimeout(300);
    this.startTask();
    this.onWrongAnswer({ skipRetry: true });
  }

  async handleCorrectAnswer() {
    await customTimeout(300);
    this.startNewTask();
    if (!this.firstUncompletedTask) {
      await customTimeout(3000);
      this.onRightAnswer();
    }
  }
}
