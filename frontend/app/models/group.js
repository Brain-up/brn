import DS from 'ember-data';
const { attr, hasMany } = DS;
import CompletionDependent from './completion-dependent';
import { reads } from '@ember/object/computed';

export default class Group extends CompletionDependent.extend({
  name: attr('string'),
  description: attr('string'),
  series: hasMany('series', { async: true }),
  children: reads('series'),
  parent: null,
  sortedSeries: reads('sortedChildren'),
  sortChildrenBy: 'id',
}) {}
