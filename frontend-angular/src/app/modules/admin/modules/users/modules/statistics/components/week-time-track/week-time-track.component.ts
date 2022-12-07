import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { BarDataType } from '@shared/components/bar-chart/models/bar-data';
import { BarOptionsType } from '@shared/components/bar-chart/models/bar-options';
import { secondsTo } from '@shared/helpers/seconds-to';
import * as dayjs from 'dayjs';
import { Dayjs } from 'dayjs';
import { IWeekChartDataItem } from '../../models/week-char-data-item';
import { DataItem } from 'billboard.js';

@Component({
  selector: 'app-week-time-track',
  templateUrl: './week-time-track.component.html',
  styleUrls: ['./week-time-track.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WeekTimeTrackComponent {
  private static readonly EXERCISING_TIME_NORM_IN_S = 20 * 60;

  private chartData: IWeekChartDataItem[];

  public readonly barOptions: BarOptionsType = {
    colors: {
      data: (dataItem) => USER_EXERCISING_PROGRESS_STATUS_COLOR[this.chartData[dataItem.index].progress],
    },
    labels: {
      format: (seconds) => (seconds ? secondsTo(seconds, 'm:s') : ''),
    },
    axis: {
      x: {
        tick: {
          format: (i: number) => `${this.chartData[i].x.toUpperCase()}\n${i + 1}`,
          culling: false,
          show: false,
        },
      },
      y: {
        tick: {
          text: {show: false},
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

  public barData: BarDataType;

  public initialIndex: number;

  @Input()
  public isLoading = true;

  @Input()
  public selectedMonth: Dayjs;

  @Input()
  public userId: number;

  public selectedDay: Dayjs;

  @Input()
  public set data(data: UserWeeklyStatistics[] | undefined) {
    if (!data) {
      return;
    }

    this.chartData = [];
    let lastDay = null;
    let lastIndex = null;
    for (let dayNumber = 1; dayNumber <= this.selectedMonth.daysInMonth(); dayNumber++) {
      const realRawItem = data.find((rawItem) => dayjs(rawItem.date).date() === dayNumber);

      if (realRawItem) {
        lastDay = dayjs(realRawItem.date);
        lastIndex = dayNumber - 1;
      }
      this.chartData.push(
        realRawItem
          ? {
            x: dayjs(realRawItem.date).format('dd'),
            y: realRawItem.exercisingTimeSeconds,
            progress: realRawItem.progress,
          }
          : {
            x: dayjs(this.selectedMonth.set('date', dayNumber)).format('dd'),
            y: 0,
            progress: 'BAD',
          }
      );
    }

    this.barData = data.length ? [['data', ...this.chartData.map((dataItem) => dataItem.y)]] : [];
    this.initialIndex = lastIndex;
    this.selectedDay = lastDay;
  }

  @Output()
  public loadPrevMonthEvent = new EventEmitter<void>();

  @Output()
  public loadNextMonthEvent = new EventEmitter<void>();

  public loadPrevMonth(): void {
    this.loadPrevMonthEvent.emit();
  }

  public loadNextMonth(): void {
    if (!this.isAllowNextMonth()) {
      return;
    }

    this.loadNextMonthEvent.emit();
  }

  public isAllowNextMonth(): boolean {
    /// for December
    if (this.selectedMonth.add(1, 'month').month() === 0 ) {
      return this.selectedMonth.month() <= dayjs().subtract(1, 'month').month();
    }
    return this.selectedMonth.add(1, 'month').month() <= dayjs().month();
  }

  onClickItem(index: number) {
    this.selectedDay = this.selectedMonth.clone().set('date', index);
  }
}
