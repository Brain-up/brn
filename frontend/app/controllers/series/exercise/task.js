import { reads } from '@ember/object/computed';
import Controller from '@ember/controller';

export default Controller.extend({
  tasks: reads('model.exercise.tasks'),
  exercise: reads('model.exercise.content'),

  transitionToNextTask() {
    this.transitionToRoute('series.exercise.task', this.nextTask);
  },
  transitionToNextExercise() {
    this.transitionToRoute('series.exercise', this.nextExercise);
  },
});
