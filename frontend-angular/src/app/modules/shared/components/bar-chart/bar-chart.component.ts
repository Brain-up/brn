import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
  ViewChild,
} from '@angular/core';
import { bar, bb, Chart, DataItem } from 'billboard.js';
import { BarDataType } from './models/bar-data';
import { BarOptionsType } from './models/bar-options';

const SELECTED_BAR_CLASS_NAME = 'selected-bar';

@Component({
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BarChartComponent implements AfterViewInit, OnDestroy {

  @Input()
  public set initialBarIndex(initialIndex: number) {
    if (!initialIndex) {
      return;
    }

    this.barIndex = initialIndex;
  }

  @Input()
  public set data(data: BarDataType) {
    if (!data) {
      return;
    }

    this.chartColumns = data;

    this.chart?.load({columns: this.chartColumns});
  }

  @Input()
  public set options(options: BarOptionsType) {
    if (!options) {
      return;
    }

    this.chartOptions = options;

    if (this.chartElemRef) {
      this.chart?.destroy();
      this.buildChart(this.clickItem);
    }
  }
  private chart: Chart;
  private chartOptions: BarOptionsType;
  private barIndex: number;
  private chartColumns: BarDataType = [];

  @ViewChild('chart')
  private chartElemRef: ElementRef;

  @Output() clickItem = new EventEmitter<number>();

  ngAfterViewInit(): void {
    this.buildChart(this.clickItem);
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private buildChart(onClickItem): void {
    this.chart = bb.generate({
      bindto: this.chartElemRef.nativeElement,

      data: {
        type: bar(),

        columns: this.chartColumns,
        colors: this.chartOptions?.colors,
        labels: this.chartOptions?.labels,
        onclick(dataItem: DataItem, element: SVGElement) {
          const childrenElements = element.parentElement.children;
          for (let i = 0; i < childrenElements.length; i++) {
            const item = childrenElements.item(i);
            item.classList.remove(SELECTED_BAR_CLASS_NAME);
          }
          element.classList.add(SELECTED_BAR_CLASS_NAME);
          onClickItem.emit(dataItem.index + 1);
        }
      },
      axis: this.chartOptions?.axis,
      grid: this.chartOptions?.grid,
      size: this.chartOptions?.size,
      legend: this.chartOptions?.legend,
      tooltip: this.chartOptions?.tooltip,
      bar: this.chartOptions?.bar,
    });

    if (this.barIndex) {
      const barItem = document.querySelector('.bb-bar-' + this.barIndex);
      if (barItem) {
        barItem.classList.add(SELECTED_BAR_CLASS_NAME);
      }
    }
  }
}
