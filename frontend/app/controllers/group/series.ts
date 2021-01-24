import Controller from '@ember/controller';
import { tracked } from '@glimmer/tracking';
import { cached } from 'tracked-toolbox';
import { task, Task } from 'ember-concurrency';
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';

export default class GroupSeriesController extends Controller {
  @service('network') network!: NetworkService;
  queryParams = ['name']
  @tracked _name = '';
  get name() {
    return this._name;
  }
  set name(value) {
    this._name = value;
    this.exerciseAvailabilityCalculationTask.perform();
  }
  @tracked
  availableExercises = [];
  // eslint-disable-next-line no-unused-vars
  @(task(function*(this: GroupSeriesController) {
    if (!this.model) {
      return;
    }
    if (!this.exerciseName) {
      return;
    }
    // @todo - fix;
    const exercises = this.model.toArray();
    const targets = exercises.filterBy('name', this.exerciseName).mapBy('id');
    const results = yield this.network.availableExercises(targets);
    this.availableExercises = results;
  }).keepLatest()) exerciseAvailabilityCalculationTask!: Task<any, any>
  get exerciseName() {
    return this.name;
  }
  @cached
  get exerciseSubGroups() {
    const exercises = this.model.toArray();
    return exercises;
  }
}
