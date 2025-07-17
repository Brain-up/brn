import { Model, belongsTo, } from 'miragejs';

export default Model.extend({
  exercise: belongsTo(),
});
