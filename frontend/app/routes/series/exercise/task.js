import Route from '@ember/routing/route';

export default Route.extend({
  afterModel(task, { to }) {
    if (
      !task.canInteract ||
      (to.parent.params.exercise_id &&
        to.parent.params.exercise_id !== task.exercise.content.id)
    ) {
      this.store
        .findRecord('exercise', to.parent.params.exercise_id)
        .then((exercise) => {
          const series = exercise.get('series');
          const firstTask = exercise.get('sortedTasks.firstObject');
          this.transitionTo(
            'series.exercise.task',
            series.get('id'),
            exercise.get('id'),
            firstTask.get('id'),
          );
        });
    }

    task.set('repetitionCount', 0);
  },
});
