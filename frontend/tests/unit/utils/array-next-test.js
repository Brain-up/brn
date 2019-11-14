import arrayNext from 'brn/utils/array-next';
import { module, test } from 'qunit';

module('Unit | Utility | array-next', function() {
  test('it works', function(assert) {
    let result = arrayNext(3, [1, 2, 3, 4, 5]);
    assert.equal(result, 4);
  });
});
