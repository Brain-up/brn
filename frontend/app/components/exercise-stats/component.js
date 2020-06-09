import Component from '@glimmer/component';
import { inject as service } from '@ember/service';

export default class ExerciseStatsComponent extends Component {
  @service('intl') intl;

  get timeStats() {
    const { endTime, startTime } = this.args.stats;
    const ms = endTime.getTime() - startTime.getTime();
    const totalSec = Math.floor(ms / 1000);
    const sec = totalSec % 60;
    const min = Math.floor(totalSec / 60);
    return { min, sec };
  }

  get repetitionIndex() {
    return this.args.stats.repetitionIndex.toFixed(2);
  }
}
