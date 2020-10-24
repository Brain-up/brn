import Component from '@glimmer/component';
import { IStatsObject } from 'brn/models/exercise';

interface IExerciseStatsComponentArgs {
  stats: IStatsObject,
  onComplete: () => void;
}

enum TrendTypes {
  positive = 'positive',
  negative = 'negative',
  neutral = 'neutral'
}

export default class ExerciseStatsComponent extends Component<IExerciseStatsComponentArgs> {
  get stats() {
    return this.args.stats || {};
  }

  get listeningsTrend() {
    const { listeningsCount, tasksCount } = this.stats;
    if (listeningsCount === tasksCount) {
      return TrendTypes.neutral;
    } else if (listeningsCount > tasksCount) {
      return TrendTypes.negative;
    } else {
      return TrendTypes.positive;
    }
  }

  get attemptsTrend() {
    const { tasksCount } = this.stats;
    const { attemptsCount } = this;
    if (attemptsCount > tasksCount) {
      return TrendTypes.negative;
    } else {
      return TrendTypes.positive;
    }
  }

  get timeStats() {
    const { endTime, startTime } = this.stats;
    const ms = endTime.getTime() - startTime.getTime();
    const totalSec = Math.floor(ms / 1000);
    const sec = totalSec % 60;
    const min = Math.floor(totalSec / 60);
    return { min, sec };
  }

  get attemptsCount() {
    return this.stats.listeningsCount;
  }

  get repetitionIndex() {
    return Number((this.stats.repetitionIndex || 0)).toFixed(2);
  }
}
