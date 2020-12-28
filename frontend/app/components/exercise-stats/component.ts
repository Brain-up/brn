import Component from '@glimmer/component';
import { IStatsExerciseStats } from 'brn/services/stats';

interface IExerciseStatsComponentArgs {
  stats: IStatsExerciseStats,
  onComplete: () => void;
}

enum TrendTypes {
  positive = 'positive',
  negative = 'negative',
  neutral = 'neutral'
}

export default class ExerciseStatsComponent extends Component<IExerciseStatsComponentArgs> {
  get stats(): IStatsExerciseStats {
    return this.args.stats || {};
  }

  get repeatsTrend() {
    if (this.stats.repeatsCount > 0) {
      return TrendTypes.negative;
    } else {
      return TrendTypes.positive;
    }
  }

  get attemptsTrend() {
    const { wrongAnswersCount } = this.stats;
    if (wrongAnswersCount > 0) {
      return TrendTypes.negative;
    } else {
      return TrendTypes.positive;
    }
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
