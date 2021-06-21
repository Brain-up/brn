import Component from '@glimmer/component';
import UserWeeklyStatisticsModel from 'brn/models/user-weekly-statistics';
import UserYearlyStatisticsModel from 'brn/models/user-yearly-statistics';
import NetworkService from 'brn/services/network';
import { inject as service } from '@ember/service';
import { DateTime } from 'luxon';
import { tracked } from '@glimmer/tracking';
import { task, Task as TaskGenerator } from 'ember-concurrency';
import { action } from '@ember/object';

export default class StatisticsComponent extends Component {
  @service('network') network!: NetworkService;

  selectedMonth: DateTime = DateTime.now();
  @tracked isLoadingWeekTimeTrackData = true;
  @tracked isLoadingMonthTimeTrackData = true;
  @tracked weekTimeTrackData: UserWeeklyStatisticsModel[] | null = null;
  @tracked monthTimeTrackData: UserYearlyStatisticsModel[] | null = null;

  @(task(function* (this: StatisticsComponent) {
    const fromMonth: Date = this.selectedMonth.startOf('month').toJSDate();
    const toMonth: Date = this.selectedMonth.endOf('month').toJSDate();
    this.isLoadingWeekTimeTrackData = true;

    this.weekTimeTrackData = yield this.network.getUserStatisticsByWeek(
      fromMonth,
      toMonth,
    );
    this.isLoadingWeekTimeTrackData = false;
  }).drop())
  getWeekTimeTrackData!: TaskGenerator<any, any>;

  @(task(function* (this: StatisticsComponent) {
    const fromYear: Date = this.selectedMonth.startOf('year').toJSDate();
    const toYear: Date = this.selectedMonth.endOf('year').toJSDate();
    this.isLoadingMonthTimeTrackData = true;

    this.monthTimeTrackData = yield this.network.getUserStatisticsByYear(
      fromYear,
      toYear,
    );
    this.isLoadingMonthTimeTrackData = false;
    if (!this.monthTimeTrackData?.length) {
      return;
    }
    const lastMonth: DateTime = DateTime.fromJSDate(
      this.monthTimeTrackData.lastObject?.date || new Date(),
    );
    if (lastMonth.month >= this.selectedMonth.month) {
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
  selectMonth(date: DateTime): void {
    this.selectedMonth = date;
    this.getWeekTimeTrackData.perform();
  }

  @action
  loadPrevYear(): void {
    this.selectedMonth = this.selectedMonth.minus({ year: 1 });

    this.getWeekTimeTrackData.perform();
    this.getMonthTimeTrackData.perform();
  }

  @action
  loadNextYear(): void {
    this.selectedMonth = this.selectedMonth.plus({ year: 1 });

    this.getWeekTimeTrackData.perform();
    this.getMonthTimeTrackData.perform();
  }
}
