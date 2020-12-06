import Controller from '@ember/controller';
import { tracked } from '@glimmer/tracking';
import { cached } from 'tracked-toolbox';
import { task, Task } from 'ember-concurrency';
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';
import Exercise from 'brn/models/exercise';

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
  @(task(function*(this: GroupSeriesController) {
    if (!this.model) {
      return;
    }
    if (!this.exerciseName) {
      return;
    }
    const exercises = this.model.exercises.toArray();
    const targets = exercises.filterBy('name', this.exerciseName).mapBy('id');
    const results = yield this.network.availableExercises(targets);
    this.availableExercises = results;
  }).keepLatest()) exerciseAvailabilityCalculationTask!: Task<any, any>
  get exerciseName() {
    return this.name;
  }
  @cached
  get exerciseGroups() {
    const items: {
      [key: string]: any
    } = {};
    const exercises = this.model.exercises.toArray();
    exercises.forEach((el: Exercise & { name: string })=>{
      if (!(el.name in items)) {
        const detail = el.name.indexOf('/') > 0 ? el.name.slice(el.name.indexOf('/'), el.name.length): '-';
        items[el.name] = {
          count: 0,
          name: el.name.replace(detail, '').trim(),
          fullName: el.name,
          detail: detail.trim(),
          picture: `/${el.pictureUrl}`
        }
      }
      items[el.name].count++;
    });
    return Object.values(items);
  }
}
