import Component from '@glimmer/component';
import { inject as service } from '@ember/service';

export default class ExerciseStatsComponent extends Component {
  @service('intl') intl;

  get minutes() {
    const { endTime, startTime } = this.args.stats;

    this.ms = endTime.getTime() - startTime.getTime();
    this.totalSec = Math.floor(this.ms / 1000);
    this.sec = this.totalSec % 60;
    this.min = Math.floor(this.totalSec / 60);
    this.minText = this.intl.t('statistics.time_min');
    this.secText = this.intl.t('statistics.time_sec');
    return `${this.min} ${this.minText} ${this.sec} ${this.secText}`;
  }

  get repetitionIndex() {
    return this.args.stats.repetitionIndex.toFixed(2);
  }
}
