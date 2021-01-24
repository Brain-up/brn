import Model, { belongsTo, hasMany, attr, AsyncHasMany, AsyncBelongsTo } from '@ember-data/model';
import Exercise from './exercise';
import Group from './group';
import { cached } from 'tracked-toolbox';
import SubgroupModel from './subgroup';
export default class Series extends Model {
  @attr('string') name!: string;
  @attr('string') description!: string;
  @attr('number') level!: number;
  @attr('string') kind!: string;
  @belongsTo('group', { async: false } ) group?: AsyncBelongsTo<Group>;
  @hasMany('subgroup', { async: false }) subGroups!:  AsyncHasMany<SubgroupModel>;
  @hasMany('exercise', { async: false } ) exercises!: AsyncHasMany<Exercise>;
  get children() {
    return this.exercises;
  }
  get parent() {
    return this.group;
  }
  set parent(value) {
    this.set('group', value);
  }
  get sortedExercises() {
    return this.exercises.sortBy('order');
  }
  get sortedChildren() {
    return this.sortedExercises;
  }

  @cached
  get groupedByNameExercises(): Record<string, Exercise[]>  {
    return this.exercises.reduce((resultObj, currentExercise) => {
      const { name } = currentExercise;
      const targetGroup = resultObj[name];
      resultObj[name] = targetGroup
        ? targetGroup.concat([currentExercise]).sortBy('order')
        : [currentExercise];

      return resultObj;
    }, {} as Record<string, Exercise[] | undefined>) as Record<string, Exercise[]>;
  }
}
