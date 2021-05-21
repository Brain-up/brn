import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { BarDataType } from '@shared/components/bar-chart/models/bar-data';
import { BarOptionsType } from '@shared/components/bar-chart/models/bar-options';
import { secondsTo } from '@shared/helpers/seconds-to';
import * as dayjs from 'dayjs';
import { Dayjs } from 'dayjs';
import { IWeekChartDataItem } from '../../models/week-char-data-item';

@Component({
  selector: 'app-week-time-track',
  templateUrl: './week-time-track.component.html',
  styleUrls: ['./week-time-track.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WeekTimeTrackComponent {
  private static readonly EXERCISING_TIME_NORM_IN_S = 20 * 60;

  private chartData: IWeekChartDataItem[];

  public barData: BarDataType;
  public barOptions: BarOptionsType = {
    colors: {
      data: (dataItem) => USER_EXERCISING_PROGRESS_STATUS_COLOR[this.chartData[dataItem.index].progress],
    },
    labels: {
      format: (seconds) => secondsTo(seconds, 'ms'),
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

  @Input()
  public isLoading = true;

  @Input()
  public selectedMonth: Dayjs;

  @Input()
  public set data(data: UserWeeklyStatistics[] | undefined) {
    if (!data) {
      return;
    }

    this.chartData = data.map((rawItem) => ({
      x: dayjs(rawItem.date).format('dd'),
      y: rawItem.exercisingTimeSeconds,
      progress: rawItem.progress,
    }));

    this.barData = [['data', ...this.chartData.map((dataItem) => dataItem.y)]];
  }
}
