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

    if (
      to.name.endsWith('exercise.index') &&
      exercise.get('sortedTasks.firstObject') &&
      !to.paramNames.includes('task_id') &&
      exercise.exerciseType !== 'WORDS_SEQUENCES'
    ) {
      this.transitionTo(
        'series.exercise.task',
        exercise.get('sortedTasks.firstObject.id'),
      );
    }

    exercise.trackTime('start');
  },
});
