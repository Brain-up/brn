import { Model, hasMany, belongsTo,} from 'ember-cli-mirage';

export default Model.extend({
  group: belongsTo(),
  exercises: hasMany(),
});
