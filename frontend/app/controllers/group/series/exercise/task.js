import Controller from '@ember/controller';
import { inject } from '@ember/service';

export default Controller.extend({
  router: inject(),
  nextTaskTransition() {
    if (!this.model.isLastTask) {
      this.router.transitionTo(
        'group.series.exercise.task',
        this.model.get('nextTask.exercise.id'),
        this.model.get('nextTask.id'),
      );
    }
  },
});
