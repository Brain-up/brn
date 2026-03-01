import Model, { attr, belongsTo, hasMany } from '@ember-data/model';
import Store from '@ember-data/store';
import { SyncHasMany, AsyncHasMany } from '@ember-data/model/-private';

export default class Group extends Model {
  @attr('string') name!: string;
  @belongsTo('series', { async: false }) series!: SeriesModel;
  @hasMany('exercise', { async: true, inverse: 'parent', polymorphic: true }) exercises!: AsyncHasMany<Exercise>;
  @hasMany('task', { async: false }) tasks!: SyncHasMany<Task>;
}

declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    group: Group;
  }
}
