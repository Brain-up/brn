import deepCopy from 'brn/utils/deep-copy';
import { module, test } from 'qunit';

module('Unit | Utility | deepCopy', function() {
  // Replace this with your real tests.
  test('it works', function(assert) {
    const testObj = {
      a: 3,
      b: {
        c: 4,
      },
    };
    let result = deepCopy(testObj);
    assert.deepEqual(result, testObj);
  });
});
