import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { task } from 'ember-concurrency';
import customTimeout from 'brn/utils/custom-timeout';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { IStatsExerciseStats } from 'brn/services/stats';

export default class GroupSeriesExerciseController extends Controller {
  @service router;
  @service tasksManager;
  @service('studying-timer') studyingTimer;
  @service('stats') stats;

  @tracked correctnessWidgetIsShown = false;
  @tracked showExerciseStats = false;
  @tracked exerciseStats = {};

  get exerciseIsCompletedInCurrentCycle() {
    return this.model.get('tasks').every((task: any) => task.get('completedInCurrentCycle'));
  }

  goToSeries() {
    this.router.transitionTo('group.series.index', this.model.get('series.id'));
  }

  get modelStats(): IStatsExerciseStats {
    return this.stats.statsFor(this.model);
  }

  saveExercise() {
    this.studyingTimer.pause();
    this.model.trackTime('end');
    this.model.postHistory(this.modelStats);
    return this.modelStats;
  }

  @(task(function*(this: GroupSeriesExerciseController, isCorrect = false) {
    const waitingTime = isCorrect ? 3000 : 2000;
    this.correctnessWidgetIsShown = true;
    yield customTimeout(waitingTime);
    this.correctnessWidgetIsShown = false;
  }).drop()) runCorrectnessWidgetTimer;


  @action
  async greedOnCompletedExercise() {
    const stats = this.saveExercise();
    await this.runCorrectnessWidgetTimer.perform(true);
    this.showExerciseStats = true;
    this.exerciseStats = stats;
  }

  @action startStatsTracking(_, [model]) {
    this.stats.registerModel(model);
  }

  @action stopStatsTracking(_, [model]) {
    this.stats.unregisterModel(model);
  }

  @action
  async afterCompleted() {
    this.showExerciseStats = false;
    this.goToSeries();
  }

  get bodyStyleNode() {
    return document.body.style;
  }

  @action
  disableBodyScroll() {
    this.bodyStyleNode.overflow = 'hidden';
  }

  @action
  enableBodyScroll() {
    this.bodyStyleNode.overflow = 'scroll';
  }
}
