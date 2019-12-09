import Route from '@ember/routing/route';

export default Route.extend({
  async afterModel(task, { to }) {
    if (
      !task.canInteract ||
      (to.parent.params.exercise_id &&
        to.parent.params.exercise_id !== task.exercise.content.id)
    ) {
      const exercise = await this.store.findRecord(
        'exercise',
        to.parent.params.exercise_id,
      );
      const series = exercise.get('series');
      const firstTask = exercise.get('sortedTasks.firstObject');
      this.transitionTo(
        'series.exercise.task',
        series.get('id'),
        exercise.get('id'),
        firstTask.get('id'),
      );
    }
  },
});
