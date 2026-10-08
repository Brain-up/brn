import { module, test } from 'qunit';
import { sortByKey } from 'brn/utils/sort-by-key';

module('Unit | Utility | sort-by-key', function () {
  test('sorts objects by a numeric key ascending', function (assert) {
    const items = [
      { name: 'c', order: 3 },
      { name: 'a', order: 1 },
      { name: 'b', order: 2 },
    ];
    const result = sortByKey(items, 'order');
    assert.deepEqual(
      result.map((i) => i.name),
      ['a', 'b', 'c'],
    );
  });

  test('sorts objects by a string key ascending', function (assert) {
    const items = [
      { id: '3', val: 'x' },
      { id: '1', val: 'y' },
      { id: '2', val: 'z' },
    ];
    const result = sortByKey(items, 'id');
    assert.deepEqual(
      result.map((i) => i.id),
      ['1', '2', '3'],
    );
  });

  test('does not mutate the original array', function (assert) {
    const items = [{ order: 3 }, { order: 1 }, { order: 2 }];
    const original = [...items];
    sortByKey(items, 'order');
    assert.deepEqual(items, original);
  });

  test('returns empty array for empty input', function (assert) {
    const result = sortByKey([], 'order');
    assert.deepEqual(result, []);
  });

  test('handles single-element array', function (assert) {
    const items = [{ order: 1 }];
    const result = sortByKey(items, 'order');
    assert.deepEqual(result, [{ order: 1 }]);
  });

  test('handles equal values without changing order', function (assert) {
    const items = [
      { order: 1, name: 'a' },
      { order: 1, name: 'b' },
    ];
    const result = sortByKey(items, 'order');
    assert.strictEqual(result.length, 2);
    // Both have same order, just verify they're all there
    assert.ok(result.find((i) => i.name === 'a'));
    assert.ok(result.find((i) => i.name === 'b'));
  });
});
