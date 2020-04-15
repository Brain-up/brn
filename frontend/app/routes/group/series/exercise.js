import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class GroupSeriesExerciseRoute extends Route {
  @service('studying-timer')
  studyingTimer;
  @service('tasks-manager')
  tasksManager;

  model({ exercise_id }) {
    return this.store.findRecord('exercise', exercise_id);
  }

  redirect(exercise, { to }) {
    if (!exercise.canInteract) {
      this.transitionTo('group.series.exercise', exercise.get('series.id'));
      return;
    }
    if (
      to.name.endsWith('exercise.index') &&
      exercise.get('sortedTasks.firstObject') &&
      !to.paramNames.includes('task_id')
    ) {
      this.transitionTo(
        'group.series.exercise.task',
        exercise.get('sortedTasks.firstObject.id'),
      );
    }
  }
  deactivate() {
    this.studyingTimer.pause();
    this.tasksManager.clearCurrentCycleTaks();
  }
}
