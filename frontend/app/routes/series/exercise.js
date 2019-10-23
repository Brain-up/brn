import Route from '@ember/routing/route';

export default Route.extend({

  model({ exercise_id }) {
    return this.store.findRecord('exercise', exercise_id, { include: 'tasks' });
  },

  async afterModel(exercise, { to }) {
    // in case if exercise was pre-loaded
    // asking for tasks again
    await exercise.hasMany('tasks').load();
    if (to.name.endsWith('exercise.index') && exercise.tasks.firstObject) {
      this.transitionTo('series.exercise.task', exercise.tasks.firstObject);
    }
  },
});
