import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import { getOwner } from '@ember/application';
import Router from '@ember/routing/router-service';
import type GroupSeriesSubgroupController from 'brn/controllers/group/series/subgroup';
import type { TaskBase } from 'brn/schemas/task';

export default class GroupSeriesSubgroupExerciseTaskController extends Controller {
  declare model: TaskBase;

  @service router!: Router;
  @action nextTaskTransition() {
    const subgroupController = getOwner(this)!.lookup(`controller:group.series.subgroup`) as GroupSeriesSubgroupController;
    subgroupController.exerciseAvailabilityCalculationTask.perform();

    if (!this.model.isLastTask) {
      const nextTask = this.model.nextTask as { exercise?: { id?: string }; id?: string } | null;
      if (nextTask?.exercise?.id && nextTask?.id) {
        this.router.transitionTo(
          'group.series.subgroup.exercise.task',
          nextTask.exercise.id,
          nextTask.id,
        );
      }
    }
  }
}
