import { ChartOptions, Data } from 'billboard.js';

export type BarOptionsType = Pick<ChartOptions, 'axis' | 'grid' | 'size' | 'legend' | 'tooltip' | 'bar'> &
  Pick<Data, 'colors' | 'labels'>;
