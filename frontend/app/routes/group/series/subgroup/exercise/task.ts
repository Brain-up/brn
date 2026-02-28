import Route from '@ember/routing/route';
import type Task from 'brn/models/task';
import type Exercise from 'brn/models/exercise';
import { inject as service } from '@ember/service';
import type Store from 'brn/services/store';
import type Router from '@ember/routing/router-service';

export default class GroupSeriesSubgroupExerciseTaskRoute extends Route {
  @service('store') store!: Store;
  @service('router') declare router: Router;

  model({ task_id }: { task_id: string }) {
    const exercise = this.modelFor('group.series.subgroup.exercise') as Exercise;
    return Array.from(exercise.tasks).find((task) => task_id === task.id);
  }
  async afterModel(task: Task, { to }: any) {
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
      await (exercise as any).hasMany('tasks').load();

      const series = exercise.series;
      const sortedTasks = exercise.sortedTasks as Task[] | null;
      const firstTask = sortedTasks?.[0];
      this.router.transitionTo(
        'group.series.subgroup.exercise.task',
        series!.id!,
        exercise.parent!.id!,
        exercise.id!,
        firstTask!.id!,
      );
    }

    task.repetitionCount = 0;
  }
}
