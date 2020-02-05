import Controller from '@ember/controller';
import { inject } from '@ember/service';
import { task } from 'ember-concurrency';
import customTimeout from 'brn/utils/custom-timeout';
import { computed } from '@ember/object';

export default Controller.extend({
  router: inject(),
  tasksManager: inject(),
  exerciseIsCompletedInCurrentCycle: computed(
    'model.tasks.@each.completedInCurrentCycle',
    function() {
      return this.model.tasks.every((task) => task.completedInCurrentCycle);
    },
  ),
  goToSeries() {
    this.router.transitionTo('group.series.index', this.model.get('series.id'));
  },
  runCorrectnessWidgetTimer: task(function*(isCorrect = false) {
    const waitingTime = isCorrect ? 3000 : 2000;
    this.set('correctnessWidgetIsShown', true);
    yield customTimeout(waitingTime);
  }).drop(),
  async afterCompleted() {
    this.saveExercise();
    await this.runCorrectnessWidgetTimer.perform();
    this.goToSeries();
  },
  saveExercise() {
    this.model.trackTime('end');
    this.model.postHistory();
  },
});
