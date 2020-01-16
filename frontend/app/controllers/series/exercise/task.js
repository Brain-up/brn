import Controller from '@ember/controller';
import { inject } from '@ember/service';

export default Controller.extend({
  router: inject(),
  nextTaskTransition() {
    !this.model.isLastTask
      ? this.router.transitionTo(
          'series.exercise.task',
          this.model.get('nextTask.exercise.id'),
          this.model.get('nextTask.id'),
        )
      : this.router.transitionTo(
          'series.index',
          this.model.get('exercise.series.id'),
        );
  },
  saveExerciseMaybe() {
    if (this.model.isLastTask && this.model.get('exercise.isCompleted')) {
      this.saveExercise(this.model.get('exercise.content'));
    }
  },
  saveExercise(exercise) {
    exercise.trackTime('end');
    exercise.postHistory();
  },
});
