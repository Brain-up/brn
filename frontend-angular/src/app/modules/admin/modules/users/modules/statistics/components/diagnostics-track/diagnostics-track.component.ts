import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import {
  Component,
  ChangeDetectionStrategy,
  Input,
  EventEmitter,
  Output,
} from '@angular/core';
import { BarDataType } from '@shared/components/bar-chart/models/bar-data';
import { LineOptionsType } from '@shared/components/line-chart/models/line-options';
import { secondsTo } from '@shared/helpers/seconds-to';
import * as dayjs from 'dayjs';
import { Dayjs } from 'dayjs';
import { IWeekChartDataItem } from '../../models/week-char-data-item';

@Component({
  selector: 'app-diagnostics-track',
  templateUrl: './diagnostics-track.component.html',
  styleUrls: ['./diagnostics-track.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DiagnosticsTrackComponent {
  private static readonly EXERCISING_TIME_NORM_IN_S = 20 * 60;

  private chartData: IWeekChartDataItem[];

  public readonly lineOptions: LineOptionsType = {
    colors: {
      data: (dataItem) =>
        USER_EXERCISING_PROGRESS_STATUS_COLOR[
          this.chartData[dataItem.index].progress
        ],
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
            value: 'WeekTimeTrackComponent.EXERCISING_TIME_NORM_IN_S',
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

  public lineData: any;

  @Input()
  public isLoading = true;

  @Input()
  public selectedMonth: Dayjs;

  @Input()
  public set data(data: any[] | undefined) {
    if (!data) {
      return;
    }

    this.chartData = [];
    for (
      let dayNumber = 1;
      dayNumber <= this.selectedMonth.daysInMonth();
      dayNumber++
    ) {
      const realRawItem = data.find(
        (rawItem) => dayjs(rawItem.date).date() === dayNumber,
      );

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
            },
      );
    }

    this.lineData = [
      {
        name: 'Germany',
        series: [
          {
            name: '1990',
            value: 62000000,
          },
          {
            name: '2010',
            value: 73000000,
          },
          {
            name: '2011',
            value: 89400000,
          },
        ],
      },

      {
        name: 'USA',
        series: [
          {
            name: '1990',
            value: 250000000,
          },
          {
            name: '2010',
            value: 309000000,
          },
          {
            name: '2011',
            value: 311000000,
          },
        ],
      },

      {
        name: 'France',
        series: [
          {
            name: '1990',
            value: 58000000,
          },
          {
            name: '2010',
            value: 50000020,
          },
          {
            name: '2011',
            value: 58000000,
          },
        ],
      },
      {
        name: 'UK',
        series: [
          {
            name: '1990',
            value: 57000000,
          },
          {
            name: '2010',
            value: 62000000,
          },
        ],
      },
    ];
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
    return this.selectedMonth.add(1, 'month').month() <= dayjs().month();
  }
}
