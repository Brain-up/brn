import { hasMany, attr, SyncHasMany } from '@ember-data/model';
import CompletionDependent from './completion-dependent';
import  SeriesModel from './series';

export default class Group extends CompletionDependent {
  parent = null;
  sortChildrenBy = 'id';

  @attr('string') name!: string;
  @attr('string') description!: string;
  @attr('string') locale!: string;
  @hasMany('series', { async: false }) series!: SyncHasMany<SeriesModel>;

  get children(): SeriesModel[] {
    return this.series.toArray();
  }
  get sortedSeries() {
    return this.sortedChildren;
  }
};
