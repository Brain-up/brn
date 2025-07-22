import { EmberDataSerializer } from "ember-cli-mirage";

export default EmberDataSerializer.extend({
  keyForModel:()=>'data',
  keyForRelationshipIds:(key)=> key,
  keyForForeignKey:(key)=> key,
});
