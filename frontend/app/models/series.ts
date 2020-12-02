import Model, { belongsTo, hasMany, attr } from '@ember-data/model';
import Exercise from './exercise';
import Group from './group';
import { cached } from 'tracked-toolbox';
export default class Series extends Model {
  @attr('string') name!: string;
  @attr('string') description!: string;
  @belongsTo('group', { async: true } ) group?: Group;
  @hasMany('exercise', { async: true } ) exercises!: Exercise[];
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
  get groupedByNameExercises() {
    return this.exercises.reduce((resultObj, currentExercise) => {
      const { name } = currentExercise;
      // @ts-expect-error
      const targetGroup = resultObj[name];
      // @ts-expect-error
      resultObj[name] = targetGroup
        ? targetGroup.concat([currentExercise]).sortBy('order')
        : [currentExercise];

      return resultObj;
    }, {});
  }
}
