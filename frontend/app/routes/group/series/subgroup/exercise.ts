import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Exercise from 'brn/models/exercise';
import type Transition from '@ember/routing/-private/transition';
import TasksManagerService from 'brn/services/tasks-manager';
import NetworkService from 'brn/services/network';
import Ember from 'ember';
import type Store from '@ember-data/store';

export default class GroupSeriesSubgroupExerciseRoute extends Route {
  @service('store') store!: Store;
  @service('tasks-manager')
  tasksManager!: TasksManagerService;
  @service('network')
  network!: NetworkService;

  isAvailable = false;

  model({ exercise_id }: { exercise_id: string }) {
    return this.store.findRecord('exercise', exercise_id);
  }

  async afterModel(exercise: Exercise) {
    const testable = await this.network.availableExercises([exercise.id]);
    this.isAvailable = testable.includes(exercise.id);
    await exercise.hasMany('tasks').load();
  }

  redirect(exercise: Exercise, { to }: Transition) {
    if (!Ember.testing && !this.isAvailable) {
      return this.transitionTo(
        'group.series.subgroup',
        exercise.get('parent.id'),
      );
    }
    if (exercise.hasMany('tasks').ids().length === 0) {
      alert(`Unable to find tasks for exercise ${exercise.get('id')}`);
      this.transitionTo('group.series', exercise.get('series.id'));
      return;
    }
    // if (!exercise.canInteract) {
    //   this.transitionTo('group.series.subgroup.exercise', exercise.get('series.id'));
    //   return;
    // }
    if (
      to.name.endsWith('exercise.index') &&
      exercise.get('sortedTasks.firstObject') &&
      !to.paramNames.includes('task_id')
    ) {
      this.transitionTo(
        'group.series.subgroup.exercise.task',
        exercise.get('sortedTasks.firstObject.id'),
      );
    }
  }
  resetController(controller, isExiting: boolean) {
    if (isExiting) {
      controller.showExerciseStats = false;
      controller.correctnessWidgetIsShown = false;
    }
  }
  deactivate() {
    this.tasksManager.clearCurrentCycleTaks();
    this.isAvailable = false;
  }
}
