import { Component, ChangeDetectionStrategy, AfterViewInit, OnDestroy, ElementRef, ViewChild, Input } from '@angular/core';
import { bar, bb, Chart } from 'billboard.js';
import { LineDataType } from './models/line-data';
import { LineOptionsType } from './models/line-options';

@Component({
  selector: 'app-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LineChartComponent implements AfterViewInit, OnDestroy {
  private chart: Chart;
  private chartOptions: LineOptionsType;
  private chartColumns: LineDataType = [];

  @ViewChild('chart')
  private chartElemRef: ElementRef;

  @Input()
  public set data(data: LineDataType) {
    if (!data) {
      return;
    }

    this.chartColumns = data;

    this.chart?.load({ columns: this.chartColumns });
  }

  @Input()
  public set options(options: LineOptionsType) {
    if (!options) {
      return;
    }

    this.chartOptions = options;

    if (this.chartElemRef) {
      this.chart?.destroy();
      this.buildChart();
    }
  }

  ngAfterViewInit(): void {
    this.buildChart();
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private buildChart(): void {
    this.chart = bb.generate({
      bindto: this.chartElemRef.nativeElement,

      data: {
        type: bar(),
        columns: this.chartColumns,
        colors: this.chartOptions?.colors,
        labels: this.chartOptions?.labels,
      },

      axis: this.chartOptions?.axis,
      grid: this.chartOptions?.grid,
      size: this.chartOptions?.size,
      legend: this.chartOptions?.legend,
      tooltip: this.chartOptions?.tooltip,
      bar: this.chartOptions?.bar,
    });
  }
}
