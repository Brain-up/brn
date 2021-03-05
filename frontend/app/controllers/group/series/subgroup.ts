import Controller from '@ember/controller';
import { tracked } from '@glimmer/tracking';
import { task, Task } from 'ember-concurrency';
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';

export default class GroupSeriesSubgroupController extends Controller {
  @service('network') network!: NetworkService;

  @tracked
  availableExercises: string[] = [];
  // eslint-disable-next-line no-unused-vars
  @(task(function*(this: GroupSeriesSubgroupController) {
    if (!this.model) {
      return;
    }
    // @todo - fix;
    const exercises = this.model.toArray();
    const targets = exercises.mapBy('id');
    const results = yield this.network.availableExercises(targets);
    this.availableExercises = results;
    return results;
  }).keepLatest()) exerciseAvailabilityCalculationTask!: Task<any, any>
}
