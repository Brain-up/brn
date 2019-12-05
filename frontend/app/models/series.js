import DS from 'ember-data';
const { attr, hasMany, belongsTo } = DS;
import { reads } from '@ember/object/computed';

export default class Series extends DS.Model.extend({
  name: attr('string'),
  description: attr('string'),
  group: belongsTo('group', { async: true }),
  exercises: hasMany('exercise', { async: true }),
  children: reads('exercises'),
  parent: reads('group'),
  sortedExercises: reads('sortedChildren'),
}) {}
