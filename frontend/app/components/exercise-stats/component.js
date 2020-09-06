import Component from '@glimmer/component';

export default class ExerciseStatsComponent extends Component {
  get stats() {
    return this.args.stats || {};
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
