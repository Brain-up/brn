import Route from '@ember/routing/route';

export default Route.extend({
  model({ exercise_id }) {
    return this.store.findRecord('exercise', exercise_id);
  },

  async afterModel(exercise, { to }) {
    await this.store.query('task', { exerciseId: exercise.id });
    if (to.name.endsWith('exercise.index') && exercise.tasks.firstObject) {
      this.transitionTo('series.exercise.task', exercise.tasks.firstObject);
    }
  },
});
