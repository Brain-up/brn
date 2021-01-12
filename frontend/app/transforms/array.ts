import Transform from '@ember-data/serializer/transform';
import { A, isArray } from '@ember/array';
import { isEmpty } from '@ember/utils';

function transformToArray(target: unknown) {
  if (isEmpty(target)) {
    return A();
  }
  return isArray(target) ? A(target as unknown[]) : A([target]);
}

const ArrayTransform = Transform.extend({
  deserialize(serialized: unknown) {
    return transformToArray(serialized);
  },

  serialize(deserialized: unknown[]) {
    return transformToArray(deserialized);
  }
});


export default ArrayTransform;

declare module 'ember-data/types/registries/transform' {
  export default interface TransformRegistry {
    'array': ArrayTransform;
  }
}
