import { Model, hasMany, belongsTo,} from 'miragejs';

export default Model.extend({
  group: belongsTo(),
  exercises: hasMany(),
});
