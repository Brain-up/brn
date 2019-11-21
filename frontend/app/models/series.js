import DS from 'ember-data';
const { attr, hasMany, belongsTo } = DS;
import CompletionDependent from './completion-dependent';
import { reads } from '@ember/object/computed';

export default class Series extends CompletionDependent.extend({
  name: attr('string'),
  description: attr('string'),
  group: belongsTo('group', { async: false }),
  exercises: hasMany('exercise', { async: true }),
  children: reads('exercises'),
  parent: reads('group'),
}) {}
