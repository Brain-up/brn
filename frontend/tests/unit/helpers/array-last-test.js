import { module, test } from 'qunit';
import { arrayLast } from 'brn/helpers/array-last';

module('Unit | Helper | array-last', function () {
  test('returns the last element of an array', function (assert) {
    assert.strictEqual(arrayLast([['a', 'b', 'c']]), 'c');
  });

  test('returns the last element of a single-element array', function (assert) {
    assert.strictEqual(arrayLast([['only']]), 'only');
  });

  test('returns undefined for an empty array', function (assert) {
    assert.strictEqual(arrayLast([[]]), undefined);
  });

  test('returns undefined for null', function (assert) {
    assert.strictEqual(arrayLast([null]), undefined);
  });

  test('returns undefined for undefined', function (assert) {
    assert.strictEqual(arrayLast([undefined]), undefined);
  });

  test('works with objects', function (assert) {
    const obj = { id: 2 };
    assert.strictEqual(arrayLast([[{ id: 1 }, obj]]), obj);
  });
});
