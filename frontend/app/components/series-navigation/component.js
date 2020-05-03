import Component from '@glimmer/component';

export default class SeriesNavigationComponent extends Component {
  get sortedExercises() {
    return this.args.exercises.sortBy('id');
  }

  get exerciseHeaders() {
    return this.sortedExercises.mapBy('name').uniq();
  }
}
