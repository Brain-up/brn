import Route from '@ember/routing/route';

export default Route.extend({
  afterModel(task, { to }) {
    if (
      !task.canInteract ||
      (to.parent.params.exercise_id &&
        to.parent.params.exercise_id !== task.exercise.content.id)
    ) {
      this.transitionTo('series.exercise', task.exercise);
    }
  },
});
