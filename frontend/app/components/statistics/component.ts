import Component from '@glimmer/component';
import UserWeeklyStatisticsModel from 'brn/models/user-weekly-statistics';
import UserYearlyStatisticsModel from 'brn/models/user-yearly-statistics';
import NetworkService from 'brn/services/network';
import { inject as service } from '@ember/service';
import { DateTime } from 'luxon';
import { tracked } from '@glimmer/tracking';
import { task, Task as TaskGenerator } from 'ember-concurrency';
import { action } from '@ember/object';
import Store from '@ember-data/store';

interface IStatisticsComponentArgs {
  initialSelectedMonth?: DateTime;
}
export default class StatisticsComponent extends Component<IStatisticsComponentArgs> {
  @service('network') network!: NetworkService;
  @service('store') store!: Store;

  @tracked selectedMonth: DateTime =
    this.args.initialSelectedMonth || DateTime.now();
  @tracked isLoadingWeekTimeTrackData = true;
  @tracked isLoadingMonthTimeTrackData = true;
  @tracked weekTimeTrackData: UserWeeklyStatisticsModel[] | null = null;
  @tracked monthTimeTrackData: UserYearlyStatisticsModel[] | null = null;
  @tracked isShownStatisticsInfoDialog: boolean = false;

  //eslint-disable-next-line
  @(task(function* (this: StatisticsComponent) {
    const fromMonth: DateTime = this.selectedMonth.startOf('month');
    const toMonth: DateTime = this.selectedMonth.endOf('month');
    this.isLoadingWeekTimeTrackData = true;

    try {
      this.weekTimeTrackData = yield this.store.query(
        'user-weekly-statistics',
        {
          from: fromMonth,
          to: toMonth,
        },
      );
    } catch (error) {
      console.error(error);
    }
    this.isLoadingWeekTimeTrackData = false;
  }).drop())
  getWeekTimeTrackData!: TaskGenerator<any, any>;

  //eslint-disable-next-line
  @(task(function* (this: StatisticsComponent) {
    const fromYear: DateTime = this.selectedMonth.startOf('year');
    const toYear: DateTime = this.selectedMonth.endOf('year');
    this.isLoadingMonthTimeTrackData = true;

    try {
      this.monthTimeTrackData = yield this.store.query(
        'user-yearly-statistics',
        {
          from: fromYear,
          to: toYear,
        },
      );
    } catch (error) {
      console.error(error);
    }
    this.isLoadingMonthTimeTrackData = false;
    if (!this.monthTimeTrackData?.length) {
      return;
    }

    const lastMonth: DateTime | null = this.monthTimeTrackData.lastObject
      ? this.monthTimeTrackData.lastObject?.date
      : null;
    if (!lastMonth) {
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
  openStatisticsInfoDialog(): void {
    this.isShownStatisticsInfoDialog = true;
  }

  @action
  hideStatisticsInfoDialog(): void {
    this.isShownStatisticsInfoDialog = false;
  }

  @action
  selectMonth(date: DateTime): void {
    this.selectedMonth = date;
    this.getWeekTimeTrackData.perform();
  }

  @action
  loadPrevYear(): void {
    this.resetCurrentData();
    this.selectedMonth = this.selectedMonth.minus({ year: 1 });
    this.getMonthTimeTrackData.perform();
  }

  @action
  loadNextYear(): void {
    this.resetCurrentData();
    this.selectedMonth = this.selectedMonth.plus({ year: 1 });
    this.getMonthTimeTrackData.perform();
  }

  resetCurrentData(): void {
    this.weekTimeTrackData = [];
    this.monthTimeTrackData = [];
  }
}
