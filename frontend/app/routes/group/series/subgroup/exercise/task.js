import Route from '@ember/routing/route';

export default class GroupSeriesSubgroupExerciseTaskRoute extends Route {
  async model({ task_id }) {
    const tasks = await this.modelFor('group.series.subgroup.exercise')
      .hasMany('tasks')
      .load();
    return tasks.toArray().find(({ id }) => task_id === id);
  }
  async afterModel(task, { to }) {
    if (
      !task.canInteract ||
      (to.parent.params.exercise_id &&
        task.exercise.content &&
        to.parent.params.exercise_id !== task.exercise.content.id)
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
