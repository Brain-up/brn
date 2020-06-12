import Route from '@ember/routing/route';

export default class GroupSeriesExerciseTaskRoute extends Route {
  async model({ task_id }) {
    const tasks = await this.modelFor('group.series.exercise').hasMany('tasks').load();
    return tasks.toArray().find(({id})=>task_id === id);
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
        'group.series.exercise.task',
        series.get('id'),
        exercise.get('id'),
        firstTask.get('id'),
      );
    }

    task.set('repetitionCount', 0);
  }
}
