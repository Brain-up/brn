import DS from 'ember-data';
import { A, isArray } from '@ember/array';
import { isEmpty } from '@ember/utils';

function transformToArray(target) {
  if (isEmpty(target)) {
    return A();
  }
  return isArray(target) ? A(target) : A([target]);
}

export default DS.Transform.extend({
  deserialize(serialized) {
    return transformToArray(serialized);
  },

  serialize(deserialized) {
    return transformToArray(deserialized);
  }
});
