import Component from '@glimmer/component';
import { IStatsExerciseStats } from 'brn/services/stats';

interface IExerciseStatsComponentArgs {
  stats: IStatsExerciseStats;
  onComplete: () => void;
}

export default class ExerciseStatsComponent extends Component<IExerciseStatsComponentArgs> {
  get stats(): IStatsExerciseStats {
    return this.args.stats || {};
  }

  get timeStats() {
    const { countedSeconds } = this.stats;
    const ms = countedSeconds * 1000;
    const totalSec = Math.floor(ms / 1000);
    const sec = totalSec % 60;
    const min = Math.floor(totalSec / 60);
    return { min, sec };
  }
}
