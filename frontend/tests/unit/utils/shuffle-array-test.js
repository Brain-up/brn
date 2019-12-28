import shuffleArray from 'brn/utils/shuffle-array';
import { module, test } from 'qunit';

module('Unit | Utility | shuffleArray', function() {
  // Replace this with your real tests.
  test('it shuffles array', function(assert) {
    const testArray = [1, 2, 3];
    let result = shuffleArray(testArray);
    assert.notDeepEqual(result, testArray);
    assert.ok(
      testArray.reduce((hasInitialArrayItems, current) => {
        hasInitialArrayItems = hasInitialArrayItems && result.includes(current);
        return hasInitialArrayItems;
      }, true),
    );
  });
});
