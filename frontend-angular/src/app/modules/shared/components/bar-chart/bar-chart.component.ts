import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import { bar, bb, Chart } from 'billboard.js';
import { BarDataType } from './models/bar-data';
import { BarOptionsType } from './models/bar-options';

@Component({
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BarChartComponent implements AfterViewInit, OnDestroy {
  private chart: Chart;
  private chartOptions: BarOptionsType;
  private chartColumns: BarDataType = [];

  @ViewChild('chart')
  private chartElemRef: ElementRef;

  @Input()
  public set data(data: BarDataType) {
    if (!data) {
      return;
    }

    this.chartColumns = data;

    this.chart?.load({ columns: this.chartColumns });
  }

  @Input()
  public set options(options: BarOptionsType) {
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
