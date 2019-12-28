import Component from '@ember/component';
import { reads } from '@ember/object/computed';
import { computed, set } from '@ember/object';
import { inject } from '@ember/service';
import deepCopy from '../../utils/deep-copy';
import { array, raw } from 'ember-awesome-macros';
import deepEqual from '../../utils/deep-equal';
import customTimeout from '../../utils/custom-timeout';

function getEmptyTemplate(selectedItemsOrder = []) {
  return selectedItemsOrder.reduce((result, currentKey) => {
    result[currentKey] = null;
    return result;
  }, {});
}

export default Component.extend({
  init() {
    this._super(...arguments);
    this.set('tasksCopy', []);
    this.startTask();
    this.updateLocalTasks();
  },
  isCorrect: false,
  checked: false,
  audio: inject(),
  currentUserObjectChoice: null,
  currentUserActionChoice: null,
  uncompletedTasks: array.filterBy('tasksCopy', raw('done'), false),
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
  updateLocalTasks() {
    const completedOrders = this.tasksCopy
      .filterBy('done', true)
      .mapBy('order');
    this.set(
      'tasksCopy',
      deepCopy(this.task.tasksToSolve).map((copy) => {
        return { ...copy, done: completedOrders.includes(copy.order) };
      }),
    );
  },
  startNewTask() {
    this.markDone(this.firstUncompletedTask);
    this.startTask();
  },
  markDone(task) {
    set(task, 'done', true);
  },
  startTask() {
    this.set('checked', false);
    this.set('isCorrect', false);
    this.set(
      'currentAnswerObject',
      getEmptyTemplate(this.task.selectedItemsOrder),
    );
  },
  async checkMaybe(selectedData) {
    this.set('currentAnswerObject', {
      ...this.currentAnswerObject,
      [selectedData.wordType]: selectedData.word,
    });
    if (this.answerCompleted) {
      this.set('checked', true);
      const isCorrect = deepEqual(
        this.task.selectedItemsOrder.map(
          (orderName) => this.currentAnswerObject[orderName],
        ),
        this.firstUncompletedTask.answer.mapBy('word'),
      );
      if (!isCorrect) {
        this.task.wrongAnswers.pushObject({
          ...this.firstUncompletedTask,
        });
      }
      this.set('isCorrect', isCorrect);

      await customTimeout(1000);

      this.updateLocalTasks();
      this.startNewTask();
    }
  },
});
