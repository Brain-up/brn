import Component from '@glimmer/component';
import {
  BarDataType,
  BarOptionsType,
} from 'brn/components/statistics/bar-chart';
import {
  PROGRESS,
  type UserExercisingProgressStatusType,
} from 'brn/schemas/user-weekly-statistics-types';
import { DateTime } from 'luxon';
import type { UserWeeklyStatistics as UserWeeklyStatisticsModel } from 'brn/schemas/user-weekly-statistics';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import { secondsTo } from 'brn/utils/seconds-to';
import { isNone } from '@ember/utils';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import didUpdate from '@ember/render-modifiers/modifiers/did-update';
import { t } from 'ember-intl';
import LoadingSpinner from 'brn/components/loading-spinner';
import StatisticsBarChart from 'brn/components/statistics/bar-chart';
import StatisticsDailyTimeTable from 'brn/components/statistics/daily-time-table';

/* eslint-disable no-unused-vars */
enum PROGRESS_COLORS {
  BAD = '#F38698',
  GOOD = '#FBD051',
  GREAT = '#47CD8A',
}
/* eslint-enable */

export interface IWeekChartDataItem {
  x: string;
  y: number;
  progress: UserExercisingProgressStatusType;
}

interface WeekTimeTrackSignature {
  Args: {
  isLoading: boolean;
  selectedMonth: DateTime;
  data: UserWeeklyStatisticsModel[];
  };
  Element: HTMLElement;
}

export default class WeekTimeTrackComponent extends Component<WeekTimeTrackSignature> {
  private static readonly EXERCISING_TIME_NORM_IN_S = 20 * 60;

  @tracked private chartData?: IWeekChartDataItem[];

  get barOptions(): BarOptionsType {
    return {
      colors: {
        data: (dataItem) => {
          const index = dataItem.index ?? 0;
          const chartItem = this.chartData?.[index];
          return chartItem ? PROGRESS_COLORS[chartItem.progress] : PROGRESS_COLORS.BAD;
        },
      },
      labels: {
        format: (seconds) => (seconds ? secondsTo(seconds, 'm:s') : ''),
      },
      axis: {
        x: {
          tick: {
            format: (i: number) => {
              const chartItem = this.chartData?.[i];
              return chartItem ? `${chartItem.x.toUpperCase()}\n${i + 1}` : `${i + 1}`;
            },
            culling: false,
            show: false,
          },
        },
        y: {
          tick: {
            text: { show: false },
            culling: false,
            show: false,
            outer: false,
          },
        },
      },
      grid: {
        y: {
          lines: [
            {
              value: WeekTimeTrackComponent.EXERCISING_TIME_NORM_IN_S,
            },
          ],
        },
      },
      size: {
        height: 200,
        width: 1000,
      },
      legend: {
        show: false,
      },
      tooltip: {
        show: false,
      },
      bar: {
        width: 16,
        radius: 8,
      },
    };
  }

  @tracked barData: BarDataType = [];

  @tracked selectedDay?: DateTime | null;

  @tracked lastBarIndex?: number | null;

  get selectedMonth(): DateTime {
    return this.args.selectedMonth;
  }

  @action
  didUpdateData(): void {
    this.chartData = [];
    const data: UserWeeklyStatisticsModel[] | null = this.args.data;
    if (isNone(data)) {
      return;
    }

    let lastDay = null;
    let lastDayIndex = -1;
    let dayNumber: number;
    for (
      dayNumber = 1;
      dayNumber <= this.selectedMonth.daysInMonth;
      dayNumber++
    ) {
      const dataItem: UserWeeklyStatisticsModel | undefined = data.find(
        (statisticsItem: UserWeeklyStatisticsModel) =>
          statisticsItem.date.day === dayNumber,
      );
      if (dataItem) {
        lastDay = dataItem.date;
        lastDayIndex = dayNumber;
      }
      this.chartData.push(
        dataItem
          ? {
              x: dataItem.date.weekdayShort.slice(0, 2),
              y: dataItem.exercisingTimeSeconds,
              progress: dataItem.progress,
            }
          : {
              x: this.selectedMonth
                .set({ day: dayNumber })
                .weekdayShort.slice(0, 2),
              y: 0,
              progress: PROGRESS.BAD,
            },
      );
    }

    this.barData = data.length
      ? [['data', ...this.chartData.map((dataItem) => dataItem.y)]]
      : [];

    if (!isNone(lastDay)) {
      this.selectedDay = lastDay;
      this.lastBarIndex = lastDayIndex - 1;
    }
  }

  @action
  onBarChartItemClicked(index: number) {
    if (index) {
       this.selectedDay = this.selectedMonth
         .set({day: index})
    }
  }

  <template>
    <div
     
      ...attributes
      {{didInsert this.didUpdateData}}
      {{didUpdate this.didUpdateData @data}}
    >
      <div class="text-xs font-semibold leading-3 uppercase">
        {{t "profile.statistics.week_time_track.title_weeks"}}
      </div>
      <div class="h-200px sm:px-5 box-content flex items-center justify-center px-2 py-6 overflow-x-auto">
        {{#if @isLoading}}
          <LoadingSpinner />
        {{else if this.barData.length}}
          <StatisticsBarChart
            @data={{this.barData}}
            @options={{this.barOptions}}
            @lastBarIndex={{this.lastBarIndex}}
            @onClickItem={{this.onBarChartItemClicked}}
          />
        {{else}}
          <div
            data-test-empty-data class="w-full text-xs font-semibold leading-3 text-center uppercase"
          >
            {{t "profile.statistics.week_time_track.empty_data" period=""}}
          </div>
        {{/if}}
      </div>
      {{#if this.selectedDay}}
        <div>
          <StatisticsDailyTimeTable
            @day={{this.selectedDay}}
          />
        </div>
      {{/if}}
    </div>
  </template>
}
