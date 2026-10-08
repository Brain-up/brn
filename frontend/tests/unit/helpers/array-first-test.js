import { module, test } from 'qunit';
import { arrayFirst } from 'brn/helpers/array-first';

module('Unit | Helper | array-first', function () {
  test('returns the first element of an array', function (assert) {
    assert.strictEqual(arrayFirst([['a', 'b', 'c']]), 'a');
  });

  test('returns the first element of a single-element array', function (assert) {
    assert.strictEqual(arrayFirst([['only']]), 'only');
  });

  test('returns undefined for an empty array', function (assert) {
    assert.strictEqual(arrayFirst([[]]), undefined);
  });

  test('returns undefined for null', function (assert) {
    assert.strictEqual(arrayFirst([null]), undefined);
  });

  test('returns undefined for undefined', function (assert) {
    assert.strictEqual(arrayFirst([undefined]), undefined);
  });

  test('works with objects', function (assert) {
    const obj = { id: 1 };
    assert.strictEqual(arrayFirst([[obj, { id: 2 }]]), obj);
  });
});
