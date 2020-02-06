import Component from '@ember/component';
import { computed } from '@ember/object';

export default Component.extend({
  tagName: '',

  sortedExercises: computed('exercises', function() {
    return this.exercises.sortBy('id');
  }),

  exerciseHeaders: computed('sortedExercises', function() {
    return this.sortedExercises.mapBy('name').uniq();
  }),
});
