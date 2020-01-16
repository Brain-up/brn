import Component from '@ember/component';
import { reads } from '@ember/object/computed';
import { computed, set } from '@ember/object';
import { inject } from '@ember/service';
import deepCopy from 'brn/utils/deep-copy';
import { array } from 'ember-awesome-macros';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';

function getEmptyTemplate(selectedItemsOrder = []) {
  return selectedItemsOrder.reduce((result, currentKey) => {
    result[currentKey] = null;
    return result;
  }, {});
}

export default Component.extend({
  didInsertElement() {
    this._super(...arguments);
    this.set('tasksCopy', []);
    this.updateLocalTasks();
    this.startTask();
  },
  isCorrect: false,
  audio: inject(),
  uncompletedTasks: computed(
    'tasksCopy',
    'tasksCopy.@each.isCompleted',
    function() {
      return this.tasksCopy.filterBy('isCompleted', false);
    },
  ),
  firstUncompletedTask: reads('uncompletedTasks.firstObject'),
  audioFiles: array.map('firstUncompletedTask.answer', (answer) => {
    return `/audio/${answer.audioFileUrl}`;
  }),
  answerCompleted: computed('currentAnswerObject', function() {
    return Object.values(this.currentAnswerObject).reduce(
      (isCompleted, currentValue) => {
        isCompleted = isCompleted && !!currentValue;
        return isCompleted;
      },
      true,
    );
  }),
  startNewTask() {
    this.markCompleted(this.firstUncompletedTask);
    this.startTask();
  },
  markCompleted(task) {
    set(task, 'isCompleted', true);
    set(task, 'nextAttempt', false);
  },
  markNextAttempt(task) {
    set(task, 'nextAttempt', true);
  },
  startTask() {
    this.set('isCorrect', false);
    this.set(
      'currentAnswerObject',
      getEmptyTemplate(this.task.selectedItemsOrder),
    );
  },
  updateLocalTasks() {
    const completedOrders = this.tasksCopy
      .filterBy('isCompleted', true)
      .mapBy('order');
    const tasksCopy = deepCopy(this.task.tasksToSolve).map((copy) => {
      const isCompleted = completedOrders.includes(copy.order);
      const copyEquivalent = this.tasksCopy.findBy('order', copy.order);
      return {
        ...copy,
        isCompleted,
        nextAttempt: copyEquivalent && !!copyEquivalent.nextAttempt,
        canInteract: true,
      };
    });
    this.set('tasksCopy', tasksCopy);
  },
  async checkMaybe(selectedData) {
    this.set('currentAnswerObject', {
      ...this.currentAnswerObject,
      [selectedData.wordType]: selectedData.word,
    });
    if (this.answerCompleted) {
      const isCorrect = deepEqual(
        this.task.selectedItemsOrder.map(
          (orderName) => this.currentAnswerObject[orderName],
        ),
        this.firstUncompletedTask.answer.mapBy('word'),
      );

      this.set('isCorrect', isCorrect);

      isCorrect
        ? await this.handleCorrectAnswer()
        : await this.handleWrongAnswer();
    }
  },

  async handleWrongAnswer() {
    this.task.wrongAnswers.pushObject({
      ...this.firstUncompletedTask,
    });
    this.markNextAttempt(this.firstUncompletedTask);
    this.updateLocalTasks();
    await customTimeout(1000);
    this.startTask();
  },

  async handleCorrectAnswer() {
    await customTimeout(1000);
    this.startNewTask();
    if (!this.firstUncompletedTask) {
      this.task.savePassed();
      this.onRightAnswer();
      await customTimeout(3000);
      this.afterCompleted();
    }
  },
});
