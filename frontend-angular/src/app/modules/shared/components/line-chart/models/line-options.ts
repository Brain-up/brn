import { ChartOptions, Data } from 'billboard.js';

export type LineOptionsType = Pick<ChartOptions, 'axis' | 'grid' | 'size' | 'legend' | 'tooltip' | 'bar'> &
  Pick<Data, 'colors' | 'labels'>;
