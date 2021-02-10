import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Exercise from 'brn/models/exercise';
import type Transition from '@ember/routing/-private/transition';
import TasksManagerService from 'brn/services/tasks-manager';

export default class GroupSeriesSubgroupExerciseRoute extends Route {
  @service('tasks-manager')
  tasksManager!: TasksManagerService;

  model({ exercise_id }: { exercise_id: string }) {
    return this.store.findRecord('exercise', exercise_id);
  }

  async afterModel(exercise: Exercise) {
    await exercise.hasMany('tasks').load();
  }

  redirect(exercise: Exercise, { to }: Transition) {
    if (exercise.hasMany('tasks').ids().length === 0) {
      alert(`Unable to find tasks for exercise ${exercise.get('id')}`);
      this.transitionTo('group.series', exercise.get('series.id'));
      return;
    }
    // if (!exercise.canInteract) {
    //   this.transitionTo('group.series.subgroup.exercise', exercise.get('series.id'));
    //   return;
    // }
    if (
      to.name.endsWith('exercise.index') &&
      exercise.get('sortedTasks.firstObject') &&
      !to.paramNames.includes('task_id')
    ) {
      this.transitionTo(
        'group.series.subgroup.exercise.task',
        exercise.get('sortedTasks.firstObject.id'),
      );
    }
  }
  resetController(controller, isExiting: boolean) {
    if (isExiting) {
      controller.showExerciseStats = false;
      controller.correctnessWidgetIsShown = false;
    }
  }
  deactivate() {
    this.tasksManager.clearCurrentCycleTaks();
  }
}
