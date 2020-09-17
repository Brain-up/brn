import Model, { belongsTo, hasMany, attr } from '@ember-data/model';

export default class Series extends Model {
  @attr('string') name;
  @attr('string') description;
  @belongsTo('group', { async: true } ) group;
  @hasMany('exercise', { async: true } ) exercises;
  get children() {
    return this.exercises;
  }
  get parent() {
    return this.group;
  }
  get sortedExercises() {
    return this.exercises.sortBy('order');
  }
  get sortedChildren() {
    return this.sortedExercises;
  }
  get groupedByNameExercises() {
    return this.exercises.reduce((resultObj, currentExercise) => {
      const { name } = currentExercise;
      const targetGroup = resultObj[name];
      resultObj[name] = targetGroup
        ? targetGroup.concat([currentExercise]).sortBy('order')
        : [currentExercise];

      return resultObj;
    }, {});
  }
}
