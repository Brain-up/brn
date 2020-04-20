import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { task } from 'ember-concurrency';
import customTimeout from 'brn/utils/custom-timeout';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';

export default class GroupSeriesExerciseController extends Controller {
  @service router;
  @service tasksManager;
  @service('studying-timer')
  studyingTimer;

  @tracked correctnessWidgetIsShown = false;
  @tracked showExerciseStats = false;

  get exerciseIsCompletedInCurrentCycle() {
    return this.model.get('tasks').every((task) => task.get('completedInCurrentCycle'));
  }

  goToSeries() {
    this.router.transitionTo('group.series.index', this.model.get('series.id'));
  }

  saveExercise() {
    this.studyingTimer.pause();
    this.model.trackTime('end');
    this.model.postHistory();
  }

  @(task(function*(isCorrect = false) {
    const waitingTime = isCorrect ? 3000 : 2000;
    this.correctnessWidgetIsShown = true;
    yield customTimeout(waitingTime);
    this.correctnessWidgetIsShown = false;
  }).drop()) runCorrectnessWidgetTimer;


  @action 
  async greedOnCompletedExercise() {
    this.saveExercise();
    await this.runCorrectnessWidgetTimer.perform(true);
    this.showExerciseStats = true;
  }

  @action
  async afterCompleted() {
    await customTimeout(5000);
    this.goToSeries();
  }
}