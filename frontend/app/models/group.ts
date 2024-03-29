import { hasMany, attr, SyncHasMany } from '@ember-data/model';
import CompletionDependent from './completion-dependent';
import SeriesModel from './series';

export default class Group extends CompletionDependent {
  parent = null;
  sortChildrenBy = 'id';

  @attr('string') name!: string;
  @attr('string') description!: string;
  @attr('string') locale!: string;
  @hasMany('series', { async: false }) series!: SyncHasMany<SeriesModel>;

  // @ts-expect-error childern owerride
  get children(): SeriesModel[] {
    return this.series.toArray();
  }
  get sortedSeries(): SeriesModel[] {
    return this.sortedChildren as unknown as SeriesModel[];
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    group: Group;
  }
}
