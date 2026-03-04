import Route from '@ember/routing/route';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import type { Exercise } from 'brn/schemas/exercise';
import type { TaskBase as Task } from 'brn/schemas/task';
import type Transition from '@ember/routing/-private/transition';
import type TasksManagerService from 'brn/services/tasks-manager';
import type NetworkService from 'brn/services/network';
import { isTesting } from '@embroider/macros';
import type Store from 'brn/services/store';
import type Router from '@ember/routing/router-service';
import type GroupSeriesSubgroupExerciseController from 'brn/controllers/group/series/subgroup/exercise';

export default class GroupSeriesSubgroupExerciseRoute extends Route {
  @service('store') store!: Store;
  @service('router') declare router: Router;
  @service('tasks-manager')
  tasksManager!: TasksManagerService;
  @service('network')
  network!: NetworkService;

  isAvailable = false;

  model({ exercise_id }: { exercise_id: string }) {
    return this.store.findRecord<Exercise>('exercise', exercise_id);
  }

  async afterModel(exercise: Exercise) {
    this.isAvailable = false;
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    const testable = await this.network.availableExercises([exercise.id!]);
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    this.isAvailable = testable.includes(exercise.id!);
    // Tasks are loaded as included resources in the exercise findRecord response,
    // so no explicit hasMany('tasks').load() is needed.
  }

  redirect(exercise: Exercise, { to }: Transition): void {
    if (!isTesting() && !this.isAvailable) {
      // Use paramsFor instead of exercise.parent (which may be null if the
      // inverse relationship wasn't populated by the cache)
      const { subgroup_id } = this.paramsFor('group.series.subgroup') as { subgroup_id: string };
      this.router.transitionTo(
        'group.series.subgroup',
        subgroup_id,
      );
      return;
    }
    const tasks = exercise.tasks || [];
    if (Array.from(tasks).length === 0) {
      console.warn(`Unable to find tasks for exercise ${exercise.id}`);
      // Use paramsFor instead of exercise.series (which may be null)
      const { series_id } = this.paramsFor('group.series') as { series_id: string };
      this.router.transitionTo('group.series', series_id);
      return;
    }
    const sortedTasks = exercise.sortedTasks as Task[] | null;
    const firstTask = sortedTasks?.[0];
    if (
      to.name.endsWith('exercise.index') &&
      firstTask &&
      !to.paramNames.includes('task_id')
    ) {
      this.router.transitionTo(
        'group.series.subgroup.exercise.task',
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        firstTask.id!,
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
