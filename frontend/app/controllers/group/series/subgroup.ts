import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import { keepLatestTask } from 'ember-concurrency';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import type NetworkService from 'brn/services/network';
import type TasksManagerService from 'brn/services/tasks-manager';

export default class GroupSeriesSubgroupController extends Controller {
  @service('network') network!: NetworkService;
  @service('tasks-manager') tasksManager!: TasksManagerService;

  @tracked
  availableExercises: string[] = [];
  @tracked _model: Iterable<{ id: string; isManuallyCompleted?: boolean }> | null = null;
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
    const exercises: Array<{ id: string; isManuallyCompleted?: boolean }> = Array.from(this.model);
    const targets = exercises.map((e) => e.id);
    const results = await this.network.availableExercises(targets);
    this.availableExercises = results as string[];

    const completedIds = this.tasksManager.completedExerciseIds;
    if (completedIds.size > 0) {
      for (const exercise of exercises) {
        if (completedIds.has(exercise.id)) {
          exercise.isManuallyCompleted = true;
        }
      }
    }
  });
}
