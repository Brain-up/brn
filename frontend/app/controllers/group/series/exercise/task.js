import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';

export default class GroupSeriesExerciseController extends Controller {
  @service router;
  @action nextTaskTransition() {
    if (!this.model.isLastTask) {
      this.router.transitionTo(
        'group.series.exercise.task',
        this.model.get('nextTask.exercise.id'),
        this.model.get('nextTask.id'),
      );
    }
  }
};
