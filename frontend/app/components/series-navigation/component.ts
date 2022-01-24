import { SyncHasMany } from '@ember-data/model';
import Component from '@glimmer/component';
import Exercise from 'brn/models/exercise';

interface ISeriesNavigationArgs {
  exercises: SyncHasMany<Exercise>;
}

export default class SeriesNavigationComponent extends Component<ISeriesNavigationArgs> {
  get sortedExercises() {
    return this.args.exercises.toArray().sortBy('level');
  }

  get exerciseHeaders() {
    return this.sortedExercises.mapBy('name').uniq();
  }
}
