import Route from '@ember/routing/route';
import type Task from 'brn/models/task';
import type Exercise from 'brn/models/exercise';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import type Store from 'brn/services/store';
import type Router from '@ember/routing/router-service';

export default class GroupSeriesSubgroupExerciseTaskRoute extends Route {
  @service('store') store!: Store;
  @service('router') declare router: Router;

  model({ task_id }: { task_id: string }) {
    const exercise = this.modelFor('group.series.subgroup.exercise') as Exercise;
    const tasks = exercise.tasks || [];
    const task = Array.from(tasks).find((t) => task_id === t.id);
    if (!task) {
      // Task not found — redirect to the exercise route which will
      // pick the first available task via its own redirect logic.
      const { exercise_id } = this.paramsFor('group.series.subgroup.exercise') as { exercise_id: string };
      this.router.transitionTo('group.series.subgroup.exercise', exercise_id);
      return;
    }
    return task;
  }

  async afterModel(task: Task | undefined, { to }: any) {
    if (!task) return;

    if (
      !task.canInteract ||
      (to.parent.params.exercise_id &&
        task.exercise &&
        to.parent.params.exercise_id !== task.exercise.id)
    ) {
      const exercise = await this.store.findRecord<Exercise>(
        'exercise',
        to.parent.params.exercise_id,
      );
      // Tasks are loaded as included resources in the exercise findRecord response,
      // so no explicit hasMany('tasks').load() is needed.

      // Use paramsFor instead of exercise.series / exercise.parent which may be null
      // if the cache hasn't populated inverse relationships
      const { series_id } = this.paramsFor('group.series') as { series_id: string };
      const { subgroup_id } = this.paramsFor('group.series.subgroup') as { subgroup_id: string };
      const sortedTasks = exercise.sortedTasks as Task[] | null;
      const firstTask = sortedTasks?.[0];
      if (firstTask) {
        this.router.transitionTo(
          'group.series.subgroup.exercise.task',
          series_id,
          subgroup_id,
          // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
          exercise.id!,
          // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
          firstTask.id!,
        );
      }
      return;
    }

    task.repetitionCount = 0;
  }
}
