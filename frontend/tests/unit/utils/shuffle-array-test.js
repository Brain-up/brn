import shuffleArray from 'brn/utils/shuffle-array';
import { module, test } from 'qunit';

module('Unit | Utility | shuffleArray', function() {
  // Replace this with your real tests.
  test('it shuffles array', function(assert) {
    const testArray = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    let result = shuffleArray(testArray);
    assert.notDeepEqual(result, testArray);
  });
});
