import Model, { attr, hasMany, type HasMany } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import type Exercise from './exercise';
import type Series from './series';
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
  exercises!: HasMany<Exercise>;
  get count(): number {
    return this.exercisesIds.length;
  }
  get parent() {
    return this.store.peekRecord<Series>('series', this.seriesId);
  }
  @cached
  get exercisesIds(): string[] {
    return (this as any).hasMany('exercises').ids();
  }
}

