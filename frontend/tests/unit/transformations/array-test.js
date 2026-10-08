import { module, test } from 'qunit';
import { ArrayTransformation } from 'brn/transformations/array';

module('Unit | Transformation | array', function () {
  module('hydrate', function () {
    test('returns empty array for null', function (assert) {
      const result = ArrayTransformation.hydrate(null, null);
      assert.deepEqual(result, []);
    });

    test('returns empty array for undefined', function (assert) {
      const result = ArrayTransformation.hydrate(undefined, null);
      assert.deepEqual(result, []);
    });

    test('returns empty array for empty array', function (assert) {
      const result = ArrayTransformation.hydrate([], null);
      assert.deepEqual(result, []);
    });

    test('wraps a non-array value in an array', function (assert) {
      const result = ArrayTransformation.hydrate('hello', null);
      assert.deepEqual(result, ['hello']);
    });

    test('wraps a numeric value in an array', function (assert) {
      const result = ArrayTransformation.hydrate(42, null);
      assert.deepEqual(result, [42]);
    });

    test('passes through a non-empty array unchanged', function (assert) {
      const input = ['a', 'b', 'c'];
      const result = ArrayTransformation.hydrate(input, null);
      assert.deepEqual(result, ['a', 'b', 'c']);
    });

    test('passes through a single-element array unchanged', function (assert) {
      const result = ArrayTransformation.hydrate(['only'], null);
      assert.deepEqual(result, ['only']);
    });
  });

  module('serialize', function () {
    test('returns empty array for null', function (assert) {
      const result = ArrayTransformation.serialize(null, null);
      assert.deepEqual(result, []);
    });

    test('returns empty array for undefined', function (assert) {
      const result = ArrayTransformation.serialize(undefined, null);
      assert.deepEqual(result, []);
    });

    test('returns empty array for empty array', function (assert) {
      const result = ArrayTransformation.serialize([], null);
      assert.deepEqual(result, []);
    });

    test('wraps a non-array value in an array', function (assert) {
      const result = ArrayTransformation.serialize('value', null);
      assert.deepEqual(result, ['value']);
    });

    test('passes through a non-empty array unchanged', function (assert) {
      const input = [1, 2, 3];
      const result = ArrayTransformation.serialize(input, null);
      assert.deepEqual(result, [1, 2, 3]);
    });
  });
});
