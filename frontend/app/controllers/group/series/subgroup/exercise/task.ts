import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import { getOwner } from '@ember/application';
import Router from '@ember/routing/router-service';

export default class GroupSeriesSubgroupExerciseTaskController extends Controller {
  @service router!: Router;
  @action nextTaskTransition() {
    getOwner(this)
      .lookup(`controller:group.series.subgroup`)
      .exerciseAvailabilityCalculationTask.perform();

    if (!this.model.isLastTask) {
      this.router.transitionTo(
        'group.series.subgroup.exercise.task',
        this.model.nextTask?.exercise?.id,
        this.model.nextTask?.id,
      );
    }
  }
}
