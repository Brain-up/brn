import Component from '@glimmer/component';
import {
  BarDataType,
  BarOptionsType,
} from 'brn/components/statistics/bar-chart/component';
import {
  PROGRESS,
  UserExercisingProgressStatusType,
} from 'brn/models/user-weekly-statistics';
import { DateTime } from 'luxon';
import UserWeeklyStatisticsModel from 'brn/models/user-weekly-statistics';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { secondsTo } from 'brn/utils/seconds-to';
import { isNone } from '@ember/utils';

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

interface IWeekTimeTrackComponentArgs {
  isLoading: boolean;
  selectedMonth: DateTime;
  data: UserWeeklyStatisticsModel[];
}

export default class WeekTimeTrackComponent extends Component<IWeekTimeTrackComponentArgs> {
  private static readonly EXERCISING_TIME_NORM_IN_S = 20 * 60;

  @tracked private chartData?: IWeekChartDataItem[];

  get barOptions(): BarOptionsType {
    return {
      colors: {
        data: (dataItem) =>
          PROGRESS_COLORS[this.chartData[dataItem.index].progress],
      },
      labels: {
        format: (seconds) => (seconds ? secondsTo(seconds, 'm:s') : ''),
      },
      axis: {
        x: {
          tick: {
            format: (i: number) =>
              `${this.chartData[i].x.toUpperCase()}\n${i + 1}`,
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
  }
}
