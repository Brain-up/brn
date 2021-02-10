import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { getOwner } from '@ember/application';
import Router from '@ember/routing/router-service';

export default class GroupSeriesSubgroupExerciseTaskController extends Controller {
  @service router!: Router;
  @action nextTaskTransition() {
    getOwner(this).lookup(`controller:group.series.subgroup`).exerciseAvailabilityCalculationTask.perform();

    if (!this.model.isLastTask) {
      this.router.transitionTo(
        'group.series.subgroup.exercise.task',
        this.model.get('nextTask.exercise.id'),
        this.model.get('nextTask.id'),
      );
    }
  }
};
