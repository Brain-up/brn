import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import { keepLatestTask } from 'ember-concurrency';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import type NetworkService from 'brn/services/network';

export default class GroupSeriesSubgroupController extends Controller {
  @service('network') network!: NetworkService;

  @tracked
  availableExercises: string[] = [];
  @tracked _model: any;
  get model() {
    return this._model;
  }
  set model(value) {
    this._model = value;
    this.exerciseAvailabilityCalculationTask.perform();
  }
  exerciseAvailabilityCalculationTask = keepLatestTask(async () => {
    if (!this.model) {
      return;
    }
    // @todo - fix;
    const exercises = Array.from(this.model);
    const targets = exercises.map((e: { id: string }) => e.id);
    const results = await this.network.availableExercises(targets);
    this.availableExercises = results as string[];
  });
}
