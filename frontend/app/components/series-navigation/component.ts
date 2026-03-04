import Component from '@glimmer/component';
import type { Exercise } from 'brn/schemas/exercise';

interface ISeriesNavigationArgs {
  exercises: Exercise[];
}

export default class SeriesNavigationComponent extends Component<ISeriesNavigationArgs> {
  get sortedExercises() {
    return Array.from(this.args.exercises).sort((a, b) => a.level - b.level);
  }

  get exerciseHeaders() {
    return [...new Set(this.sortedExercises.map((e) => e.name))];
  }
}
