import Component from '@glimmer/component';

export default class SeriesNavigationComponent extends Component {
  get sortedExercises() {
    return this.args.exercises.toArray().sortBy('level');
  }

  get exerciseHeaders() {
    return this.sortedExercises.mapBy('name').uniq();
  }
}
