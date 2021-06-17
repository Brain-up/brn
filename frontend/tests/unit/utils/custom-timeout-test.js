import customTimeout from 'brn/utils/custom-timeout';
import { settled } from '@ember/test-helpers';
import { module, test } from 'qunit';

module('Unit | Utility | custom-timeout', function () {
  // Replace this with your real tests.
  test('is less than 100ms for test environment', async function (assert) {
    await settled();
    const startTime = new Date().getTime();
    await customTimeout();
    const endTime = new Date().getTime();
    assert.ok(endTime - startTime < 100);
  });
});
