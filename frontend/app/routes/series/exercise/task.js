import checkInteractionRoute from '../../check-interaction';

export default checkInteractionRoute.extend({
  afterModel(task, { to }) {
    this._super(...arguments);
    if (
      to.parent.params.exercise_id &&
      to.parent.params.exercise_id !== task.exercise.content.id
    ) {
      this.transitionTo('series.exercise', task.exercise.content);
    }
  },
});
