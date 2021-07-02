import { action } from '@ember/object';
import Component from '@glimmer/component';
import { trackedRef } from 'ember-ref-bucket';
import type { Chart, ChartOptions, Data } from 'billboard.js';
import { isNone } from '@ember/utils';

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
  @trackedRef('chartContainer') chartElemRef!: HTMLDivElement;

  get chartOptions(): BarOptionsType {
    return this.args.options;
  }

  get chartColumns(): BarDataType {
    return this.args.data;
  }

  @action
  didUpdateData() {
    if (isNone(this.chartColumns)) {
      return;
    }
    this.chart?.load({ columns: this.chartColumns });
  }

  @action
  didUpdateOptions() {
    if (isNone(this.chartOptions)) {
      return;
    }
    this.chart?.destroy();
    if (this.chartElemRef) {
      this.buildChart();
    }
  }

  @action
  async buildChart() {
    const { bar, bb } = await import('billboard.js');
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
