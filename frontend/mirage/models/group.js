import { Model, hasMany, } from 'miragejs';

export default Model.extend({
  series: hasMany(),
});
