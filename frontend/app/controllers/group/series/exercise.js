import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { task } from 'ember-concurrency';
import customTimeout from 'brn/utils/custom-timeout';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';

export default class GroupSeriesExerciseController extends Controller {
  @service router;
  @service tasksManager;

  @tracked correctnessWidgetIsShown = false;

  get exerciseIsCompletedInCurrentCycle() {
    return this.model.get('tasks').every((task) => task.get('completedInCurrentCycle'));
  }

  goToSeries() {
    this.router.transitionTo('group.series.index', this.model.get('series.id'));
  }

  saveExercise() {
    this.model.trackTime('end');
    this.model.postHistory();
  }

  @(task(function*(isCorrect = false) {
    const waitingTime = isCorrect ? 3000 : 2000;
    this.set('correctnessWidgetIsShown', true);
    yield customTimeout(waitingTime);
  }).drop()) runCorrectnessWidgetTimer;

  @action
  async afterCompleted() {
    this.saveExercise();
    await this.runCorrectnessWidgetTimer.perform();
    this.goToSeries();
  }
}