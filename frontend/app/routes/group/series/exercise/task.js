import Route from '@ember/routing/route';
import { dasherize } from '@ember/string';

export default Route.extend({
  async model({ task_id }) {
    const defaultTask = await this.store.findRecord('task', task_id);
    const modelType = dasherize(defaultTask.exerciseType);
    let task = await this.store.findRecord(`task/${modelType}`, task_id);
    return task;
  },
  async afterModel(task, { to }) {
    if (
      !task.canInteract ||
      (to.parent.params.exercise_id &&
        task.exercise.content &&
        to.parent.params.exercise_id !== task.exercise.content.id)
    ) {
      const exercise = await this.store.findRecord(
        'exercise',
        to.parent.params.exercise_id,
      );

      const series = exercise.get('series');
      const firstTask = exercise.get('sortedTasks.firstObject');
      this.transitionTo(
        'group.series.exercise.task',
        series.get('id'),
        exercise.get('id'),
        firstTask.get('id'),
      );
    }

    task.set('repetitionCount', 0);
  },
});
