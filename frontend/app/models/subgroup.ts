import Model, { attr, hasMany, AsyncHasMany } from '@ember-data/model';
import Exercise from './exercise';
import { cached } from 'tracked-toolbox';

export default class SubgroupModel extends Model {
  @attr('string') seriesId!: string;
  @attr('string') name!: string;
  @attr('number') level!: number;
  @attr('string') pictureUrl!: string;
  get picture() {
    return `/${this.pictureUrl}`;
  }
  @attr('string') description!: string;
  @hasMany('exercise', { async: false, inverse: 'parent' }) exercises!: AsyncHasMany<Exercise>;
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
