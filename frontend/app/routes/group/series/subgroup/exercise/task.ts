import Route from '@ember/routing/route';
import Task from 'brn/models/task';

export default class GroupSeriesSubgroupExerciseTaskRoute extends Route {
  async model({ task_id }: { task_id: string }) {
    const tasks = await this.modelFor('group.series.subgroup.exercise').tasks;
    return tasks.toArray().find(({ id }) => task_id === id);
  }
  async afterModel(task: Task, { to }) {
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

      const series = exercise.get('series');
      const firstTask = exercise.get('sortedTasks.firstObject');
      this.transitionTo(
        'group.series.subgroup.exercise.task',
        series.get('id'),
        exercise.get('subGroupId'),
        exercise.get('id'),
        firstTask.get('id'),
      );
    }

    task.set('repetitionCount', 0);
  }
}
