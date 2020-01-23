import DS from 'ember-data';
const { attr, hasMany, belongsTo } = DS;
import { computed } from '@ember/object';
import { reads } from '@ember/object/computed';

export default class Series extends DS.Model.extend({
  name: attr('string'),
  description: attr('string'),
  group: belongsTo('group', { async: true }),
  exercises: hasMany('exercise', { async: true }),
  children: reads('exercises'),
  parent: reads('group'),
  sortedExercises: computed('exercises.{[],@each.order}', function() {
    return this.exercises.sortBy('order');
  }),
  sortedChildren: reads('sortedExercises'),
  groupedByNameExercises: computed('exercises.{[],@each.name}', function() {
    return this.exercises.reduce((resultObj, currentExercise) => {
      const { name } = currentExercise;
      const targetGroup = resultObj[name];
      resultObj[name] = targetGroup
        ? targetGroup.concat([currentExercise]).sortBy('order')
        : [currentExercise];

      return resultObj;
    }, {});
  }),
}) {}
