import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
// import { getOwner } from '@ember/application';

export default class GroupSeriesExerciseController extends Controller {
  @service router;
  @action nextTaskTransition() {
    // getOwner(this).lookup(`controller:group.series`).exerciseAvailabilityCalculationTask.perform();

    if (!this.model.isLastTask) {
      this.router.transitionTo(
        'group.series.subgroup.exercise.task',
        this.model.get('nextTask.exercise.id'),
        this.model.get('nextTask.id'),
      );
    }
  }
};
