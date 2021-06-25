import deepEqual from 'brn/utils/deep-equal';
import { module, test } from 'qunit';
import testSample from './test-support/test-sample';

module('Unit | Utility | deep-equal', function () {
  test('returns true if objects are equal', function (assert) {
    const sampleCopy = JSON.parse(JSON.stringify(testSample));
    assert.ok(deepEqual(testSample, sampleCopy));
  });
  test('returns false if objects are not equal', function (assert) {
    let sampleCopy = JSON.parse(JSON.stringify(testSample));
    assert.notOk(deepEqual(testSample, { ...sampleCopy, test: true }));
  });
});
