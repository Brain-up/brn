import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { task } from 'ember-concurrency';
import customTimeout from 'brn/utils/custom-timeout';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import StatsService, { IStatsExerciseStats } from 'brn/services/stats';
import Router from '@ember/routing/router-service';
import TasksManagerService from 'brn/services/tasks-manager';
import StudyingTimerService from 'brn/services/studying-timer';
import Exercise from 'brn/models/exercise';

export default class GroupSeriesExerciseController extends Controller {
  @service('router') router!: Router;
  @service('tasks-manager') tasksManager!: TasksManagerService;
  @service('studying-timer') studyingTimer!: StudyingTimerService;
  @service('stats') stats!: StatsService;

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


  enableNextExercise(model: Exercise) {
    // to-do add integration test for it
    const children = model.get('parent.groupedByNameExercises')[this.model.name];
    const index = children.indexOf(this.model);
    const nextIndex = index + 1;
    this.model.set('isManuallyCompleted', true);

    if (children[nextIndex]) {
      children[nextIndex].set('available', true);
    }
  }

  @action
  async afterCompleted() {
    this.showExerciseStats = false;

    this.enableNextExercise(this.model as Exercise);

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
