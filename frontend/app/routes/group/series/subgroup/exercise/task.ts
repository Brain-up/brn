import Route from '@ember/routing/route';
import Task from 'brn/models/task';
import { inject as service } from '@ember/service';
import type { Store } from '@warp-drive-mirror/core';
import type Router from '@ember/routing/router-service';

export default class GroupSeriesSubgroupExerciseTaskRoute extends Route {
  @service('store') store!: Store;
  @service('router') declare router: Router;

  model({ task_id }: { task_id: string }) {
    const tasks = this.modelFor('group.series.subgroup.exercise').tasks;
    return Array.from(tasks).find(({ id }: { id: string }) => task_id === id);
  }
  async afterModel(task: Task, { to }: any) {
    if (
      !task.canInteract ||
      (to.parent.params.exercise_id &&
        task.exercise &&
        to.parent.params.exercise_id !== task.exercise.id)
    ) {
      const exercise = await this.store.findRecord(
        'exercise',
        to.parent.params.exercise_id,
      );
      await exercise.hasMany('tasks').load();

      const series = exercise.series;
      const sortedTasks = exercise.sortedTasks;
      const firstTask = sortedTasks?.[0];
      this.router.transitionTo(
        'group.series.subgroup.exercise.task',
        series?.id,
        exercise.parent?.id,
        exercise.id,
        firstTask?.id,
      );
    }

    task.repetitionCount = 0;
  }
}
