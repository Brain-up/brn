import Model, { attr, hasMany, SyncHasMany } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import Exercise from './exercise';
import { cached } from 'tracked-toolbox';

export default class Subgroup extends Model {
  declare [Type]: 'subgroup';
  @attr('string') seriesId!: string;
  @attr('string') name!: string;
  @attr('number') level!: number;
  @attr('string') pictureUrl!: string;
  get picture() {
    return this.pictureUrl;
  }
  @attr('string') description!: string;
  @hasMany('exercise', { async: false, inverse: 'parent' })
  exercises!: SyncHasMany<Exercise>;
  get count() {
    return this.exercisesIds.length;
  }
  get parent() {
    return this.store.peekRecord('series', this.seriesId);
  }
  @cached
  get exercisesIds() {
    return this.hasMany('exercises').ids();
  }
}

