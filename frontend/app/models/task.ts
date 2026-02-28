import { belongsTo, attr } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import { isEmpty } from '@ember/utils';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import CompletionDependent from './completion-dependent';
import arrayNext from 'brn/utils/array-next';
import Exercise from './exercise';
import TasksManagerService from 'brn/services/tasks-manager';
import StudyingTimerService from 'brn/services/studying-timer';
import { tracked } from '@glimmer/tracking';
import AnswerOption from 'brn/utils/answer-option';
import { ExerciseMechanism } from 'brn/utils/exercise-types';
export default class Task extends CompletionDependent {
  // Union includes subclass types to satisfy WarpDrive's [Type] brand variance requirements
  declare [Type]: 'task' | 'task/signal' | 'task/single-simple-words' | 'task/words-sequences';
  get usePreGeneratedAudio() {
    return this.exercise.audioFileUrlGenerated;
  }

  @service('tasks-manager') tasksManager!: TasksManagerService;
  @service('studying-timer') studyingTimer!: StudyingTimerService;

  @attr('string') name!: string;
  @attr('string') exerciseType!: string;
  @attr('string') exerciseMechanism!: ExerciseMechanism;

  @attr('number') order!: number;
  @attr('number') repetitionCount!: number;

  @attr('boolean') shouldBeWithPictures!: boolean;

  @attr() answerOptions!: any;
  @attr({
    defaultValue() {
      return [];
    },
  })
  normalizedAnswerOptions!: AnswerOption[];

  @belongsTo('exercise', {
    async: false,
    inverse: 'tasks',
    polymorphic: true,
    as: 'task',
  })
  exercise!: Exercise;

  @tracked
  _completedInCurrentCycle = false;
  @tracked
  nextAttempt = false;
  get parent() {
    return this.exercise;
  }
  set parent(value) {
    this.exercise = value;
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
  set completedInCurrentCycle(value) {
    this._completedInCurrentCycle = value;
  }
  get nextTask() {
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

