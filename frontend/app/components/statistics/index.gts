import Component from '@glimmer/component';
import type { UserWeeklyStatistics as UserWeeklyStatisticsModel } from 'brn/schemas/user-weekly-statistics';
import type { UserYearlyStatistics as UserYearlyStatisticsModel } from 'brn/schemas/user-yearly-statistics';
import NetworkService from 'brn/services/network';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import { DateTime } from 'luxon';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import { dropTask } from 'ember-concurrency';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import type Store from 'brn/services/store';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import { on } from '@ember/modifier';
import { t } from 'ember-intl';
import UiHelp from 'brn/components/ui/help';
import StatisticsMonthTimeTrack from 'brn/components/statistics/month-time-track';
import StatisticsWeekTimeTrack from 'brn/components/statistics/week-time-track';
import StatisticsInfoDialog from 'brn/components/statistics/info-dialog';

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

  getWeekTimeTrackData = dropTask(async () => {
    const fromMonth: DateTime = this.selectedMonth.startOf('month');
    const toMonth: DateTime = this.selectedMonth.endOf('month');
    this.isLoadingWeekTimeTrackData = true;

    try {
      this.weekTimeTrackData = await this.store.query<UserWeeklyStatisticsModel>(
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
  });

  getMonthTimeTrackData = dropTask(async () => {
    const fromYear: DateTime = this.selectedMonth.startOf('year');
    const toYear: DateTime = this.selectedMonth.endOf('year');
    this.isLoadingMonthTimeTrackData = true;

    try {
      this.monthTimeTrackData = await this.store.query<UserYearlyStatisticsModel>(
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
  });

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
    <div class="overflow-hidden" ...attributes {{didInsert this.onInit}}>
      <div class="flex justify-end mb-2">
        <button
          data-test-help-button
          type="button"
          class="btn-press hover:bg-gray-300 text-purple-primary active:bg-gray-400 focus:outline-none flex items-center px-4 py-2 text-xs font-bold tracking-wider uppercase border-2 border-gray-200 rounded-full shadow-lg"
          {{on "click" this.openStatisticsInfoDialog}}
        >
          <UiHelp class="mr-2" />
          {{t "profile.statistics.about"}}
        </button>
      </div>
      <StatisticsMonthTimeTrack
        @isLoading={{this.isLoadingMonthTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @onSelectMonth={{this.selectMonth}}
        @onLoadPrevYear={{this.loadPrevYear}}
        @onLoadNextYear={{this.loadNextYear}}
        @data={{this.monthTimeTrackData}}
      />
      <StatisticsWeekTimeTrack
        @isLoading={{this.isLoadingWeekTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.weekTimeTrackData}}
      />
      {{#if this.isShownStatisticsInfoDialog}}
        <StatisticsInfoDialog
          @closeModalAction={{this.hideStatisticsInfoDialog}}
        />
      {{/if}}
    </div>
  </template>
}
