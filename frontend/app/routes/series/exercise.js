import checkInteractionRoute from '../check-interaction';

export default checkInteractionRoute.extend({
  model({ exercise_id }) {
    return this.store.findRecord('exercise', exercise_id);
  },

  async afterModel(exercise, { to }) {
    this._super(...arguments);
    await this.store.query('task', { exerciseId: exercise.id });
    if (
      to.name.endsWith('exercise.index') &&
      exercise.tasks.firstObject &&
      !to.paramNames.includes('task_id')
    ) {
      this.transitionTo('series.exercise.task', exercise.tasks.firstObject);
    }
  },
});
