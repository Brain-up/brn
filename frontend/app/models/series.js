import DS from 'ember-data';
const { Model, attr, hasMany, belongsTo } = DS;

export default Model.extend({
  name: attr('string'),
  description: attr('string'),
  group: belongsTo('group', { async: true }),
  exercises: hasMany('exercise', { async: true }),
});
