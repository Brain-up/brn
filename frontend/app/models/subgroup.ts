import Model, { attr, hasMany, AsyncHasMany } from '@ember-data/model';
import Exercise from './exercise';

export default class SubgroupModel extends Model {
  @attr('string') seriesId!: string;
  @attr('string') name!: string;
  @attr('number') level!: number;
  @attr('string') pictureUrl!: string;
  @attr('string') description!: string;
  @hasMany('exercise') exercises!: AsyncHasMany<Exercise>
}
