import { hasMany, attr } from '@ember-data/model';
import CompletionDependent from './completion-dependent';
import  SeriesModel from './series';

export default class Group extends CompletionDependent {
  parent = null;
  sortChildrenBy = 'id';

  @attr('string') name!: string;
  @attr('string') description!: string;
  @hasMany('series', { async: false }) series!: SeriesModel[];

  // @ts-expect-error
  get children() {
    return this.series.toArray();
  }
  get sortedSeries() {
    return this.sortedChildren;
  }
};
