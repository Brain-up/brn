import Model, { attr, belongsTo, hasMany } from '@warp-drive/legacy/model';
import type Store from 'brn/services/store';
import { Type } from '@warp-drive/core/types/symbols';

export default class Group extends Model {
  declare [Type]: 'group';
  @attr('string') name!: string;
  @belongsTo('series', {
    async: false,
    inverse: null
  }) series!: SeriesModel;
  @hasMany('exercise', { async: false, inverse: 'parent', polymorphic: true }) exercises!: Exercise[];
  @hasMany('task', {
    async: false,
    inverse: null
  }) tasks!: Task[];
}
