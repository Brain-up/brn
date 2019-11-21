import DS from 'ember-data';
const { attr, hasMany } = DS;
import CompletionDependent from './completion-dependent';

export default class Group extends CompletionDependent.extend({
  name: attr('string'),
  description: attr('string'),
  series: hasMany('series', { async: true }),
}) {}
