import Component from '@ember/component';
import { set, action } from '@ember/object';
import { inject as service } from '@ember/service';
import deepCopy from 'brn/utils/deep-copy';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';
import { TaskItem } from 'brn/utils/task-item';
import { tracked } from '@glimmer/tracking';
import { urlForAudio } from 'brn/utils/file-url';
import { MODES } from 'brn/utils/task-modes';
import { task } from 'ember-concurrency';

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
    return (
      this.firstUncompletedTask &&
      this.firstUncompletedTask.answer.map(({ audioFileUrl }) => {
        return urlForAudio(audioFileUrl);
      })
    );
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
    if (this.mode === MODES.TASK) {
      this.audio.startPlayTask(this.audioFiles);
    }
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

  @(task(function*(selected) {
    this.currentAnswerObject = {
      ...this.currentAnswerObject,
      [selected.wordType]: selected.word,
    };
    if (this.answerCompleted) {
      const isCorrect = deepEqual(
        this.task.selectedItemsOrder.map(
          (orderName) => this.currentAnswerObject[orderName],
        ),
        this.firstUncompletedTask.answer.mapBy('word'),
      );

      this.isCorrect = isCorrect;

      if (isCorrect) {
        yield this.handleCorrectAnswer();
      } else {
        yield this.handleWrongAnswer();
      }
    }
  }).drop())
  showTaskResult;

  @action
  async checkMaybe(selectedData) {
    this.showTaskResult.perform(selectedData);
  }

  async handleWrongAnswer() {
    this.task.wrongAnswers.pushObject(this.firstUncompletedTask.serialize());
    this.markNextAttempt(this.firstUncompletedTask);
    this.updateLocalTasks();
    await customTimeout(1000);
    this.startTask();
    this.onWrongAnswer({ skipRetry: true });
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
