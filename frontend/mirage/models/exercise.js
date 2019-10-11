import { Model, belongsTo, hasMany, } from 'ember-cli-mirage';

export default Model.extend({
  series: belongsTo(),
  tasks: hasMany(),
});
