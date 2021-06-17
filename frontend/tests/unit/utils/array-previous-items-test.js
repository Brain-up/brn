import arrayPreviousItems from 'brn/utils/array-previous-items';
import { module, test } from 'qunit';

module('Unit | Utility | arrayPreviousItems', function () {
  // Replace this with your real tests.
  test('it returns previous items in the given list', function (assert) {
    const testArray = [1, 2, 3, 4];
    let result = arrayPreviousItems(3, testArray);
    assert.deepEqual(result, [1, 2]);
  });
});
