import DS from 'ember-data';
const { Model, attr, belongsTo } = DS;
import { tag } from 'ember-awesome-macros';

export default Model.extend({
  name: attr('string'),
  word: attr('string'),
  order: attr('number'),
  audioFileId: attr('string'),
  words: attr('array'),
  exercise: belongsTo('exercise', {async: true}),

  audioFileUrl: tag`/audio/${'audioFileId'}`,
});
