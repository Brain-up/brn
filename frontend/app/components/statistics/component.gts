import Component from '@glimmer/component';
import type { UserWeeklyStatistics as UserWeeklyStatisticsModel } from 'brn/schemas/user-weekly-statistics';
import type { UserYearlyStatistics as UserYearlyStatisticsModel } from 'brn/schemas/user-yearly-statistics';
import NetworkService from 'brn/services/network';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import { DateTime } from 'luxon';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { task, Task as TaskGenerator } from 'ember-concurrency';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import type Store from 'brn/services/store';

interface StatisticsSignature {
  Args: {
  initialSelectedMonth?: DateTime;
  };
  Element: HTMLElement;
}

export default class StatisticsComponent extends Component<StatisticsSignature> {
  @service('network') network!: NetworkService;
  @service('store') store!: Store;

  @tracked selectedMonth: DateTime =
    this.args.initialSelectedMonth || DateTime.now();
  @tracked isLoadingWeekTimeTrackData = true;
  @tracked isLoadingMonthTimeTrackData = true;
  @tracked weekTimeTrackData: UserWeeklyStatisticsModel[] | null = null;
  @tracked monthTimeTrackData: UserYearlyStatisticsModel[] | null = null;
  @tracked isShownStatisticsInfoDialog = false;

  //eslint-disable-next-line
  @(task(function* (this: StatisticsComponent) {
    const fromMonth: DateTime = this.selectedMonth.startOf('month');
    const toMonth: DateTime = this.selectedMonth.endOf('month');
    this.isLoadingWeekTimeTrackData = true;

    try {
      this.weekTimeTrackData = yield this.store.query<UserWeeklyStatisticsModel>(
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
      this.monthTimeTrackData = yield this.store.query<UserYearlyStatisticsModel>(
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

    const lastItem = this.monthTimeTrackData[this.monthTimeTrackData.length - 1];
    const lastMonth: DateTime | null = lastItem ? lastItem.date : null;
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

  <template>
    <div class="overflow-hidden" ...attributes {{did-insert this.onInit}}>
      <div class="flex justify-end mb-2">
        <button
          data-test-help-button
          type="button"
          class="btn-press hover:bg-gray-300 text-purple-primary active:bg-gray-400 focus:outline-none flex items-center px-4 py-2 text-xs font-bold tracking-wider uppercase border-2 border-gray-200 rounded-full shadow-lg"
          {{on "click" this.openStatisticsInfoDialog}}
        >
          <Ui::Help class="mr-2" />
          {{t "profile.statistics.about"}}
        </button>
      </div>
      <Statistics::MonthTimeTrack
        @isLoading={{this.isLoadingMonthTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @onSelectMonth={{this.selectMonth}}
        @onLoadPrevYear={{this.loadPrevYear}}
        @onLoadNextYear={{this.loadNextYear}}
        @data={{this.monthTimeTrackData}}
      />
      <Statistics::WeekTimeTrack
        @isLoading={{this.isLoadingWeekTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.weekTimeTrackData}}
      />
      {{#if this.isShownStatisticsInfoDialog}}
        <Statistics::InfoDialog
          @closeModalAction={{this.hideStatisticsInfoDialog}}
        />
      {{/if}}
    </div>
  </template>
}
