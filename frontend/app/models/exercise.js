import DS from 'ember-data';
const { Model, attr, hasMany, belongsTo } = DS;

export default Model.extend({
  name: attr('string'),
  description: attr('string'),
  series: belongsTo('series', {async: true}),
  tasks: hasMany('task', {async: true}),
});
