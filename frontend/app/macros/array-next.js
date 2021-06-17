import arrayNext from 'brn/utils/array-next';
import computed from 'ember-macro-helpers/computed';

export default function (itemKey, arrayKey) {
  return computed(itemKey, arrayKey, function (item, array) {
    return arrayNext(item, array);
  });
}
