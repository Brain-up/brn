import Component from '@glimmer/component';
import { inject as service } from '@ember/service';

export default class ExerciseStatsComponent extends Component {
  @service('intl') intl;

  get minutes() {
    const { endTime, startTime } = this.args.stats;
    this.exerciseTime = (
      (endTime.getTime() - startTime.getTime()) /
      1000 /
      60
    ).toFixed(2);
    this.timerMin = this.intl.t('statistics.time_min');
    this.timerSec = this.intl.t('statistics.time_sec');

    return (
      this.exerciseTime.replace('.', ` ${this.timerMin} `) +
      ` ${this.timerSec} `
    );
  }

  get repetitionIndex() {
    return this.args.stats.repetitionIndex.toFixed(2);
  }
}
