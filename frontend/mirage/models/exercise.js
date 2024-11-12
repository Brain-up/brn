import { Model, belongsTo, hasMany, } from 'miragejs';

export default Model.extend({
  series: belongsTo(),
  tasks: hasMany(),
});
