import Component from '@glimmer/component';
import {
  BarDataType,
  BarOptionsType,
} from 'brn/components/statistics/bar-chart/component';
import { UserExercisingProgressStatusType } from 'brn/models/user-weekly-statistics';
import { DateTime } from 'luxon';
import UserWeeklyStatisticsModel from 'brn/models/user-weekly-statistics';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { secondsTo } from 'brn/utils/seconds-to';

enum PROGRESS_COLORS {
  BAD = '#F38698',
  GOOD = '#FBD051',
  GREAT = '#47CD8A',
}

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

  @tracked barData: BarDataType;

  get selectedMonth(): DateTime {
    return this.args.selectedMonth;
  }

  @action
  didUpdateData(): void {
    this.chartData = [];
    const data = this.args.data;
    if (!data) {
      return;
    }

    for (
      let dayNumber = 1;
      dayNumber <= this.selectedMonth.daysInMonth;
      dayNumber++
    ) {
      const realRawItem = data.find(
        (rawItem) => DateTime.fromISO(rawItem.date).day === dayNumber,
      );
      this.chartData.push(
        realRawItem
          ? {
              x: DateTime.fromISO(realRawItem.date).weekdayShort.slice(0, 2),
              y: realRawItem.exercisingTimeSeconds,
              progress: realRawItem.progress,
            }
          : {
              x: this.selectedMonth
                .set({ day: dayNumber })
                .weekdayShort.slice(0, 2),
              y: 0,
              progress: 'BAD',
            },
      );
    }

    this.barData = data.length
      ? [['data', ...this.chartData.map((dataItem) => dataItem.y)]]
      : [];
  }
}
