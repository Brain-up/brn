import { module, test } from 'qunit';
import { PROGRESS } from 'brn/schemas/user-weekly-statistics-types';

module('Unit | Schema | user-weekly-statistics-types', function () {
  test('PROGRESS.BAD has correct value', function (assert) {
    assert.strictEqual(PROGRESS.BAD, 'BAD');
  });

  test('PROGRESS.GOOD has correct value', function (assert) {
    assert.strictEqual(PROGRESS.GOOD, 'GOOD');
  });

  test('PROGRESS.GREAT has correct value', function (assert) {
    assert.strictEqual(PROGRESS.GREAT, 'GREAT');
  });

  test('PROGRESS contains exactly three members', function (assert) {
    const values = Object.values(PROGRESS);
    assert.strictEqual(values.length, 3);
    assert.deepEqual(values.sort(), ['BAD', 'GOOD', 'GREAT']);
  });
});
