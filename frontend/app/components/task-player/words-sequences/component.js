import Component from '@ember/component';
import { set } from '@ember/object';
import { inject as service } from '@ember/service';
import deepCopy from 'brn/utils/deep-copy';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';
import { TaskItem } from 'brn/utils/task-item';
import { tracked } from '@glimmer/tracking';

function getEmptyTemplate(selectedItemsOrder = []) {
  return selectedItemsOrder.reduce((result, currentKey) => {
    result[currentKey] = null;
    return result;
  }, {});
}

export default class WordsSequencesComponent extends Component {
  tagName = '';
  didInsertElement() {
    this.updateLocalTasks();
	  this.startTask();
  }
  @service audio;
  @tracked
  tasksCopy = [];
  @tracked
  currentAnswerObject = null;
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
  get audioFiles() {
    return this.firstUncompletedTask.answer.map(({ audioFileUrl }) => {
      return `/audio/${audioFileUrl}`;
    });
  }
  get answerCompleted() {
    return Object.values(this.currentAnswerObject).reduce(
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
  markCompleted(task) {
    set(task, 'completedInCurrentCycle', true);
    set(task, 'nextAttempt', false);
  }
  markNextAttempt(task) {
    set(task, 'nextAttempt', true);
  }
  startTask() {
    this.isCorrect = false;
    this.currentAnswerObject = getEmptyTemplate(this.task.selectedItemsOrder);
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
        this.firstUncompletedTask.answer.mapBy('word'),
      );

      this.isCorrect = isCorrect;

      isCorrect
        ? await this.handleCorrectAnswer()
        : await this.handleWrongAnswer();
    }
  }

  async handleWrongAnswer() {
    this.task.wrongAnswers.pushObject(this.firstUncompletedTask.serialize());
    this.markNextAttempt(this.firstUncompletedTask);
    this.updateLocalTasks();
    await customTimeout(1000);
    this.startTask();
  }

  async handleCorrectAnswer() {
    await customTimeout(1000);
    this.startNewTask();
    if (!this.firstUncompletedTask) {
      this.task.savePassed();
      this.onRightAnswer();
      await customTimeout(3000);
      this.afterCompleted();
    }
  }
}
