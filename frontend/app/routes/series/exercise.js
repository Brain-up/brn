import Route from '@ember/routing/route';

export default Route.extend({
  model({ exercise_id }) {
    return this.store.findRecord('exercise', exercise_id);
  },

  async afterModel(exercise, { to }) {
    if (!exercise.canInteract) {
      this.transitionTo('series.exercise', exercise.get('series.id'));
      return;
    }

    await this.store.query('task', { exerciseId: exercise.id });
    if (
      to.name.endsWith('exercise.index') &&
      exercise.get('sortedTasks.firstObject') &&
      !to.paramNames.includes('task_id')
    ) {
      this.transitionTo(
        'series.exercise.task',
        exercise.get('sortedTasks.firstObject.id'),
      );
    }

    exercise.trackTime('start');
  },
});
