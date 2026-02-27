import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Exercise from 'brn/models/exercise';
import type Transition from '@ember/routing/-private/transition';
import TasksManagerService from 'brn/services/tasks-manager';
import NetworkService from 'brn/services/network';
import Ember from 'ember';
import type { Store } from '@warp-drive-mirror/core';
import type Router from '@ember/routing/router-service';
import GroupSeriesSubgroupExerciseController from 'brn/controllers/group/series/subgroup/exercise';

export default class GroupSeriesSubgroupExerciseRoute extends Route {
  @service('store') store!: Store;
  @service('router') declare router: Router;
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
      return this.router.transitionTo(
        'group.series.subgroup',
        exercise.parent?.id,
      );
    }
    if (exercise.hasMany('tasks').ids().length === 0) {
      alert(`Unable to find tasks for exercise ${exercise.id}`);
      this.router.transitionTo('group.series', exercise.series?.id);
      return;
    }
    const sortedTasks = exercise.sortedTasks;
    const firstTask = sortedTasks?.[0];
    if (
      to.name.endsWith('exercise.index') &&
      firstTask &&
      !to.paramNames.includes('task_id')
    ) {
      this.router.transitionTo(
        'group.series.subgroup.exercise.task',
        firstTask.id,
      );
    }
  }
  resetController(controller: GroupSeriesSubgroupExerciseController, isExiting: boolean) {
    if (isExiting) {
      controller.showExerciseStats = false;
      controller.exerciseStats = {};
      controller.correctnessWidgetIsShown = false;
    }
  }
  deactivate() {
    this.tasksManager.clearCurrentCycleTaks();
    this.isAvailable = false;
  }
}
