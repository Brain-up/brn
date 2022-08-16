import { action } from '@ember/object';
import Component from '@glimmer/component';
import type {Chart, ChartOptions, Data, DataItem} from 'billboard.js';
import { isNone } from '@ember/utils';
import {tracked} from "@glimmer/tracking";

const SELECTED_BAR_CLASS_NAME = 'selected-bar';

export type BarDataType = [...[string, ...number[]][]];

export type BarOptionsType = Pick<
  ChartOptions,
  'axis' | 'grid' | 'size' | 'legend' | 'tooltip' | 'bar'
> &
  Pick<Data, 'colors' | 'labels'>;

interface IBarChartComponentArgs {
  data: BarDataType;
  options: BarOptionsType;
  lastBarIndex: number | null;
  onClickItem(index: number): void;
}

export default class BarChartComponent extends Component<IBarChartComponentArgs> {
  private chart: Chart | undefined;
  get chartElemRef(): HTMLDivElement {
    return document.getElementById('chart') as HTMLDivElement;
  }
  get chartOptions(): BarOptionsType {
    return this.args.options;
  }

  get chartColumns(): BarDataType {
    return this.args.data;
  }

  @tracked selectedIndex?: number | null;

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
    const onClickItemFunction = this.args.onClickItem;
    const { bar, bb } = await import('billboard.js');
    if (!this.chartElemRef) {
      return;
    }
    this.chart = bb.generate({
      bindto: this.chartElemRef,

      data: {
        type: bar(),
        columns: this.chartColumns,
        colors: this.chartOptions?.colors,
        labels: this.chartOptions?.labels,
        onclick(dataItem: DataItem, element: SVGElement) {
          const childrenElements = element?.parentElement?.children;
          if (childrenElements) {
            for (let i = 0; i < childrenElements.length; i++) {
              const item = childrenElements.item(i);
              item?.classList.remove(SELECTED_BAR_CLASS_NAME);
            }
            element.classList.add(SELECTED_BAR_CLASS_NAME);
            onClickItemFunction(dataItem.index + 1);
          }
        }
      },

      axis: this.chartOptions?.axis,
      grid: this.chartOptions?.grid,
      size: this.chartOptions?.size,
      legend: this.chartOptions?.legend,
      tooltip: this.chartOptions?.tooltip,
      bar: this.chartOptions?.bar,
    });

    if (!isNone(this.args.lastBarIndex)) {
      const barItem = this.chartElemRef.querySelector('.bb-bar-' + this.args.lastBarIndex);
      barItem?.classList.add(SELECTED_BAR_CLASS_NAME);
    }
  }

  @action
  onWillDestroy() {
    this.chart?.destroy();
  }
}
