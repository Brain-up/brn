import Component from '@glimmer/component';
import UserWeeklyStatisticsModel from 'brn/models/user-weekly-statistics';
import UserYearlyStatisticsModel from 'brn/models/user-yearly-statistics';
import NetworkService from 'brn/services/network';
import { inject as service } from '@ember/service';
import moment from 'moment';
import { tracked } from '@glimmer/tracking';
import { task, Task as TaskGenerator } from 'ember-concurrency';
import { action } from '@ember/object';

export default class StatisticsComponent extends Component {
  @service('network') network!: NetworkService;

  selectedMonth: moment.Moment = moment();
  @tracked isLoadingWeekTimeTrackData = true;
  @tracked isLoadingMonthTimeTrackData = true;
  @tracked weekTimeTrackData: UserWeeklyStatisticsModel[] | null = null;
  @tracked monthTimeTrackData: UserYearlyStatisticsModel[] | null = null;

  @(task(function* (this: StatisticsComponent) {
    const fromMonth: Date = this.selectedMonth
      .clone()
      .startOf('month')
      .toDate();
    const toMonth: Date = this.selectedMonth.clone().endOf('month').toDate();
    this.isLoadingWeekTimeTrackData = true;

    this.weekTimeTrackData = yield this.network.getUserStatisticsByWeek(
      fromMonth,
      toMonth,
    );
    this.isLoadingWeekTimeTrackData = false;
  }).drop())
  getWeekTimeTrackData!: TaskGenerator<any, any>;

  @(task(function* (this: StatisticsComponent) {
    const fromYear: Date = this.selectedMonth.clone().startOf('year').toDate();
    const toYear: Date = this.selectedMonth.clone().endOf('year').toDate();
    this.isLoadingMonthTimeTrackData = true;

    this.monthTimeTrackData = yield this.network.getUserStatisticsByYear(
      fromYear,
      toYear,
    );
    this.isLoadingMonthTimeTrackData = false;
    if (!this.monthTimeTrackData?.length) {
      return;
    }
    const lastMonth: moment.Moment = moment(
      this.monthTimeTrackData.lastObject?.date,
    );
    if (lastMonth.month() >= this.selectedMonth.month()) {
      return;
    }
    this.selectedMonth = lastMonth;
    this.getWeekTimeTrackData.perform();
  }).drop())
  getMonthTimeTrackData!: TaskGenerator<any, any>;

  @action
  onInit(): void {
    this.getWeekTimeTrackData.perform();
    this.getMonthTimeTrackData.perform();
  }

  @action
  openStatisticsInfoDialog(): void {}

  @action
  selectMonth(date: moment.Moment): void {
    this.selectedMonth = date;
    this.getWeekTimeTrackData.perform();
  }

  @action
  loadPrevYear(): void {
    this.selectedMonth = this.selectedMonth.subtract(1, 'year');

    this.getWeekTimeTrackData.perform();
    this.getMonthTimeTrackData.perform();
  }

  @action
  loadNextYear(): void {
    this.selectedMonth = this.selectedMonth.add(1, 'year');

    this.getWeekTimeTrackData.perform();
    this.getMonthTimeTrackData.perform();
  }
}
