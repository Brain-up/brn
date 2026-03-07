import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import { dropTask } from 'ember-concurrency';
import customTimeout from 'brn/utils/custom-timeout';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import type StatsService from 'brn/services/stats';
import type { IStatsExerciseStats } from 'brn/services/stats';
import type GamificationService from 'brn/services/gamification';
import Router from '@ember/routing/router-service';
import type TasksManagerService from 'brn/services/tasks-manager';
import type StudyingTimerService from 'brn/services/studying-timer';
import type { Exercise } from 'brn/schemas/exercise';
import { getOwner } from '@ember/application';
import type GroupSeriesSubgroupController from 'brn/controllers/group/series/subgroup';

export default class GroupSeriesSubgroupExerciseController extends Controller {
  declare model: Exercise;

  @service('router') router!: Router;
  @service('tasks-manager') tasksManager!: TasksManagerService;
  @service('studying-timer') studyingTimer!: StudyingTimerService;
  @service('stats') stats!: StatsService;
  @service('gamification') gamification!: GamificationService;

  @tracked correctnessWidgetIsShown = false;
  @tracked showExerciseStats = false;
  @tracked exerciseStats = {};

  get exerciseIsCompletedInCurrentCycle() {
    const tasksArray = Array.from(this.model.tasks);
    if (tasksArray.length === 0) return false;
    return tasksArray.every((task: any) => task.completedInCurrentCycle);
  }

  goToSeries() {
    this.router.transitionTo('group.series.subgroup', this.model.parent.id);
  }

  get modelStats(): IStatsExerciseStats {
    return this.stats.statsFor(this.model);
  }

  saveExercise() {
    this.studyingTimer.pause();
    this.model.trackTime('end');
    this.model.postHistory(this.modelStats);
    this.gamification.completeExercise({
      wrongAnswersCount: this.modelStats.wrongAnswersCount,
      countedSeconds: this.modelStats.countedSeconds,
    });
    return this.modelStats;
  }

  runCorrectnessWidgetTimer = dropTask(async (isCorrect = false) => {
    const waitingTime = isCorrect ? 3000 : 2000;
    this.correctnessWidgetIsShown = true;
    await customTimeout(waitingTime);
    this.correctnessWidgetIsShown = false;
  });

  @action
  async greedOnCompletedExercise() {
    const stats = this.saveExercise();
    await this.runCorrectnessWidgetTimer.perform(true);
    this.showExerciseStats = true;
    this.exerciseStats = stats;
  }

  @action startStatsTracking(_element: unknown, [model]: [Exercise]) {
    this.stats.registerModel(model);
    this.gamification.resetSession();
  }

  @action stopStatsTracking(_element: unknown, [model]: [Exercise]) {
    this.stats.unregisterModel(model);
  }

  enableNextExercise(model: Exercise) {
    // to-do add integration test for it
    const children = Array.from(model.parent.exercises);
    const index = children.indexOf(this.model as unknown as typeof children[number]);
    const nextIndex = index + 1;
    model.isManuallyCompleted = true;

    if (children[nextIndex]) {
      children[nextIndex].available = true;
    }
  }

  @action
  async afterCompleted() {
    this.enableNextExercise(this.model);

    const subgroupController = getOwner(this)!.lookup(`controller:group.series.subgroup`) as GroupSeriesSubgroupController;
    await subgroupController.exerciseAvailabilityCalculationTask.perform();
    this.showExerciseStats = false;
    this.exerciseStats = {};
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
    this.bodyStyleNode.overflow = 'auto';
  }
}
