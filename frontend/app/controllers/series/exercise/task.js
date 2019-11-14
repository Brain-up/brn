import { reads } from '@ember/object/computed';
import Controller from '@ember/controller';
import arrayNext from 'brn/macros/array-next';
import { bool } from 'ember-awesome-macros';

export default Controller.extend({
  tasks: reads('model.exercise.tasks'),
  exercise: reads('model.exercise.content'),
  siblingExercises: reads('model.exercise.content.series.content.children'),
  nextTask: arrayNext('model', 'tasks'),
  hasNextTask: bool('nextTask'),
  nextExercise: arrayNext('exercise', 'siblingExercises'),
  hasNextExercise: bool('nextExercise'),

  transitionToNextTask() {
    this.transitionToRoute('series.exercise.task', this.nextTask);
  },
  transitionToNextExercise() {
    this.transitionToRoute('series.exercise', this.nextExercise);
  },
});
