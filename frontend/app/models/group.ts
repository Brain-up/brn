import { hasMany, attr, type HasMany } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import CompletionDependent from './completion-dependent';
import SeriesModel from './series';

export default class Group extends CompletionDependent {
  declare [Type]: 'group';
  parent = null;
  sortChildrenBy = 'id';

  @attr('string') name!: string;
  @attr('string') description!: string;
  @attr('string') locale!: string;
  @hasMany('series', { async: false, inverse: 'group' }) series!: HasMany<SeriesModel>;

  // @ts-expect-error children override
  get children(): SeriesModel[] {
    return Array.from(this.series);
  }
  get sortedSeries(): SeriesModel[] {
    return this.sortedChildren as unknown as SeriesModel[];
  }
}

