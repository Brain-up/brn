import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  ViewChild,
} from '@angular/core';
import { bb, bar, Chart } from 'billboard.js';
import { LEVEL_COLOR } from '../../models/level-color';
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
  public set data(data: any) {
    this.chartData = [
      { x: 'ПН', y: Math.random() * 200 },
      { x: 'ВТ', y: Math.random() * 200 },
      { x: 'СР', y: Math.random() * 200 },
      { x: 'ЧТ', y: Math.random() * 200 },
      { x: 'ПТ', y: Math.random() * 200 },
    ];

    this.chart?.load({
      columns: [['data', ...this.chartData.map((dataItem) => dataItem.y)]],
    });
  }

  @Output()
  public loadPrevWeekEvent = new EventEmitter<void>();

  @Output()
  public loadNextWeekEvent = new EventEmitter<void>();

  @ViewChild('chart')
  private chartElemRef: ElementRef;

  ngAfterViewInit(): void {
    this.chart = bb.generate({
      bindto: this.chartElemRef.nativeElement,
      data: {
        columns: [['data', ...this.chartData.map((dataItem) => dataItem.y)]],
        type: bar(),
        colors: {
          data: (dataItem) => {
            if (dataItem.value < 30) {
              return LEVEL_COLOR[0];
            }

            if (dataItem.value < 70) {
              return LEVEL_COLOR[1];
            }

            return LEVEL_COLOR[2];
          },
        },
        labels: {
          format: (value) => value + ':' + '00',
        },
      },
      axis: {
        x: {
          tick: {
            format: (i: number) => `${this.chartData[i].x}\n${i}`,
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

  public loadPrevWeek(): void {
    this.loadPrevWeekEvent.emit();
  }

  public loadNextWeek(): void {
    this.loadNextWeekEvent.emit();
  }
}
