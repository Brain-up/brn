import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, Input, ViewChild } from '@angular/core';
import { secondsTo } from '@shared/helpers/seconds-to';
import { bb, bar, Chart } from 'billboard.js';
import * as dayjs from 'dayjs';
import { IWeekChartDataItem } from '../../models/week-char-data-item';

@Component({
  selector: 'app-week-time-track',
  templateUrl: './week-time-track.component.html',
  styleUrls: ['./week-time-track.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WeekTimeTrackComponent implements AfterViewInit {
  private chart: Chart;
  private chartData: IWeekChartDataItem[];

  @Input()
  public set data(data: UserWeeklyStatistics[] | null) {
    if (!data) return;

    this.chartData = data.map((rawItem) => ({
      x: dayjs(rawItem.date).format('dd'),
      y: rawItem.exercisingTimeSeconds,
      progress: rawItem.progress,
    }));

    this.chart?.load({
      columns: [['data', ...this.chartData.map((dataItem) => dataItem.y)]],
    });
  }

  @ViewChild('chart')
  private chartElemRef: ElementRef;

  ngAfterViewInit(): void {
    this.chart = bb.generate({
      bindto: this.chartElemRef.nativeElement,
      data: {
        columns: [['data', ...(this.chartData?.map((dataItem) => dataItem.y) ?? [])]],
        type: bar(),
        colors: {
          data: (dataItem) => USER_EXERCISING_PROGRESS_STATUS_COLOR[this.chartData?.[dataItem.index].progress],
        },
        labels: {
          format: (seconds) => secondsTo(seconds, 'ms'),
        },
      },
      axis: {
        x: {
          tick: {
            format: (i: number) => `${this.chartData?.[i].x}\n${i}`,
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
              value: 50,
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
    });
  }
}
