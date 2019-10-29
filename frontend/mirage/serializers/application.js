import { RestSerializer, } from 'ember-cli-mirage';

export default RestSerializer.extend({
  keyForModel:()=>'data',
  keyForRelationshipIds:(key)=> key,
  keyForForeignKey:(key)=> key,
});
