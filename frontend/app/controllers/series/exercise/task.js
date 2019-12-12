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
      : '';
  },
  saveExerciseMaybe() {
    if (
      this.model.isLastExerciseTask &&
      this.model.get('exercise.isCompleted')
    ) {
      this.saveExercise();
    }
  },
  saveExercise() {
    this.model.exercise.content.save();
  },
});
