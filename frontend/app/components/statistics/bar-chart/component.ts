import { action } from '@ember/object';
import Component from '@glimmer/component';
import { trackedRef } from 'ember-ref-bucket';
import { bar, bb, Chart } from 'billboard.js';
import { ChartOptions, Data } from 'billboard.js';

export type BarDataType = [...[string, ...number[]][]];

export type BarOptionsType = Pick<
  ChartOptions,
  'axis' | 'grid' | 'size' | 'legend' | 'tooltip' | 'bar'
> &
  Pick<Data, 'colors' | 'labels'>;

interface IBarChartComponentArgs {
  data: BarDataType;
  options: BarOptionsType;
}

export default class BarChartComponent extends Component<IBarChartComponentArgs> {
  private chart: Chart | undefined;
  private chartOptions: BarOptionsType | undefined;
  private chartColumns: BarDataType | undefined;
  @trackedRef('chartContainer') chartElemRef!: HTMLDivElement;

  @action
  didUpdateData() {
    this.data = this.args.data;
  }

  @action
  didUpdateOptions() {
    this.options = this.args.options;
  }

  set data(data: BarDataType) {
    if (!data) {
      return;
    }
    this.chartColumns = data;
    this.chart?.load({ columns: this.chartColumns });
  }

  set options(options: BarOptionsType) {
    if (!options) {
      return;
    }
    this.chartOptions = options;
    if (this.chartElemRef) {
      this.chart?.destroy();
      this.buildChart();
    }
  }

  @action
  buildChart(): void {
    this.chart = bb.generate({
      bindto: this.chartElemRef,

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

  @action
  onWillDestroy() {
    this.chart?.destroy();
  }
}
