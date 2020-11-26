import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class GroupSeriesExerciseRoute extends Route {
  @service('tasks-manager')
  tasksManager;

  model({ exercise_id }) {
    return this.store.findRecord('exercise', exercise_id);
  }

  redirect(exercise, { to }) {
    if (exercise.hasMany('tasks').ids().length === 0) {
      alert(`Unable to find tasks for exercise ${exercise.get('id')}`);
      this.transitionTo('group.series', exercise.get('series.id'));
      return;
    }
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
  resetController(controller, isExiting) {
    if (isExiting) {
      controller.showExerciseStats = false;
      controller.correctnessWidgetIsShown = false;
    }
  }
  deactivate() {
    this.tasksManager.clearCurrentCycleTaks();
  }
}
