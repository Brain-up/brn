import Route from '@ember/routing/route';

export default Route.extend({
  afterModel(task, { to }) {
    if (
      !task.canInteract ||
      (to.parent.params.exercise_id &&
        to.parent.params.exercise_id !== task.exercise.content.id)
    ) {
      const exercise = task.get('exercise');
      const series = exercise.get('series');
      this.transitionTo(
        'series.exercise.task',
        series.get('id'),
        exercise.get('id'),
      );
    }
  },
});
