import DS from 'ember-data';
const { attr, hasMany, belongsTo } = DS;
import { inject as service } from '@ember/service';
import CompletionDependent from './completion-dependent';
import { reads } from '@ember/object/computed';

export default class Exercise extends CompletionDependent.extend({
  name: attr('string'),
  description: attr('string'),
  series: belongsTo('series', { async: true }),
  tasks: hasMany('task', { async: true }),
  tasksManager: service(),
  children: reads('tasks'),
  parent: reads('series'),
}) {}
