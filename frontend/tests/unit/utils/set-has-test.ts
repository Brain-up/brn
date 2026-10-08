import { setHas } from 'brn/utils/set-has';
import { module, test } from 'qunit';

module('Unit | Utility | set-has', function () {
  test('returns true when the set contains the value', function (assert) {
    const set = new Set(['apple', 'banana', 'cherry']);
    assert.true(setHas(set, 'banana'), 'setHas returns true for a value in the set');
  });

  test('returns false when the set does not contain the value', function (assert) {
    const set = new Set(['apple', 'banana', 'cherry']);
    assert.false(setHas(set, 'grape'), 'setHas returns false for a value not in the set');
  });

  test('returns false when the set is undefined', function (assert) {
    assert.false(setHas(undefined, 'anything'), 'setHas returns false when set is undefined');
  });

  test('returns false when the set is empty', function (assert) {
    const set = new Set<string>();
    assert.false(setHas(set, 'anything'), 'setHas returns false for an empty set');
  });

  test('returns false when the set is null (runtime safety)', function (assert) {
    // Glimmer template args could pass null at runtime even if types say undefined
    assert.false(setHas(null as any, 'anything'), 'setHas returns false when set is null');
  });

  test('handles empty string as a value', function (assert) {
    const set = new Set(['', 'a']);
    assert.true(setHas(set, ''), 'setHas returns true for empty string in set');
    assert.false(setHas(new Set(['a']), ''), 'setHas returns false for empty string not in set');
  });
});
