import Component from 'brn/components/task-player/words-sequences/component';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';
import { set, action } from '@ember/object';
import { urlForAudio } from 'brn/utils/file-url';
import deepCopy from 'brn/utils/deep-copy';
import { TaskItem } from 'brn/utils/task-item';

export default class SingleSimpleWordsComponent extends Component {
  tagName = '';
  didInsertElement() {
    this.updateLocalTasks();
    this.startTask();
  }
  @service audio;
  @tracked
  tasksCopy = [];
  @tracked
  currentAnswer = null;
  @tracked
  isCorrect = false;
  get uncompletedTasks() {
    return this.tasksCopy.filter(
      ({ completedInCurrentCycle }) => completedInCurrentCycle === false,
    );
  }
  get firstUncompletedTask() {
    return this.uncompletedTasks.firstObject;
  }
  get audioFileUrl() {
    return (
      this.firstUncompletedTask &&
      urlForAudio(this.firstUncompletedTask.answer.audioFileUrl)
    );
  }

  startNewTask() {
    this.markCompleted(this.firstUncompletedTask);
    this.startTask();
  }
  markCompleted(task) {
    set(task, 'completedInCurrentCycle', true);
    set(task, 'nextAttempt', false);
  }
  markNextAttempt(task) {
    set(task, 'nextAttempt', true);
  }
  startTask() {
    this.isCorrect = false;
  }
  updateLocalTasks() {
    const completedOrders = this.tasksCopy
      .filterBy('completedInCurrentCycle', true)
      .mapBy('order');
    const tasksCopy = deepCopy(this.task.tasksToSolve).map((copy) => {
      const completedInCurrentCycle = completedOrders.includes(copy.order);
      const copyEquivalent = this.tasksCopy.findBy('order', copy.order);
      return new TaskItem({
        ...copy,
        completedInCurrentCycle,
        nextAttempt: copyEquivalent && !!copyEquivalent.nextAttempt,
        canInteract: true,
      });
    });
    this.tasksCopy = tasksCopy;
  }
  @action
  async checkMaybe(selectedData) {
    this.currentAnswer = selectedData;
    const isCorrect = deepEqual(
      this.currentAnswer,
      this.firstUncompletedTask.answer.word,
    );

    this.isCorrect = isCorrect;

    isCorrect
      ? await this.handleCorrectAnswer()
      : await this.handleWrongAnswer();
  }

  async handleWrongAnswer() {
    this.task.wrongAnswers.pushObject(this.firstUncompletedTask.serialize());
    this.markNextAttempt(this.firstUncompletedTask);
    this.updateLocalTasks();
    await customTimeout(1000);
    this.startTask();
    this.onWrongAnswer();
  }

  async handleCorrectAnswer() {
    await customTimeout(1000);
    this.startNewTask();
    if (!this.firstUncompletedTask) {
      await customTimeout(3000);
      this.onRightAnswer();
    }
  }
}
