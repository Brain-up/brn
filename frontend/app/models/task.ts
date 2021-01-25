import { belongsTo, attr, AsyncBelongsTo } from '@ember-data/model';
import { isEmpty } from '@ember/utils';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import CompletionDependent from './completion-dependent';
import arrayNext from 'brn/utils/array-next';
import Exercise from './exercise';
import TasksManagerService from 'brn/services/tasks-manager';
import StudyingTimerService from 'brn/services/studying-timer';
import { tracked } from '@glimmer/tracking';
export default class Task extends CompletionDependent {
  @service('tasks-manager') tasksManager!: TasksManagerService;
  @service('studying-timer') studyingTimer!: StudyingTimerService;

  @attr('string') name!: string;
  @attr('string') exerciseType!: string;

  @attr('number') order!: number;
  @attr('number') repetitionCount!: number;

  @attr() answerOptions!: any;
  // @ts-ignore
  @attr('', { defaultValue() { return [];}}) normalizedAnswerOptions!: any;

  @belongsTo('exercise', {
    async: false,
    inverse: 'tasks',
    polymorphic: true,
  }) exercise!: AsyncBelongsTo<Exercise>;

  @tracked
  _completedInCurrentCycle = false;
  @tracked
  nextAttempt = false;
  get parent() {
    return this.exercise;
  }
  set parent(value) {
    this.set('exercise', value);
  }
  get pauseExecution() {
    return this.studyingTimer.isPaused;
  }
  get isCompleted() {
    return this.tasksManager.isCompleted(this);
  }
  get completedInCurrentCycle() {
    return (
      this._completedInCurrentCycle ||
      this.tasksManager.isCompletedInCurrentCycle(this)
    );
  }
  set completedInCurrentCycle (value) {
    this._completedInCurrentCycle = value;
  }
  get nextTask() {
    // @ts-ignore
    return arrayNext(this, this.exercise.sortedChildren);
  }

  get isLastTask() {
    return isEmpty(this.nextTask);
  }


  @action
  savePassed() {
    return this.tasksManager.saveAsCompleted(this);
  }
}
