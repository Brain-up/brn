import arrayNext from 'brn/utils/array-next';
import computed from 'ember-macro-helpers/computed';

export default function (itemKey: string, arrayKey: string): unknown {
  return computed(itemKey, arrayKey, function (item: unknown, array: unknown) {
    return arrayNext(item, array as ArrayLike<unknown>);
  });
}
