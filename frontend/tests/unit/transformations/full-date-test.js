import { module, test } from 'qunit';
import { FullDateTransformation } from 'brn/transformations/full-date';
import { DateTime } from 'luxon';

module('Unit | Transformation | full-date', function () {
  module('hydrate', function () {
    test('returns null for null', function (assert) {
      const result = FullDateTransformation.hydrate(null, null);
      assert.strictEqual(result, null);
    });

    test('returns null for undefined', function (assert) {
      const result = FullDateTransformation.hydrate(undefined, null);
      assert.strictEqual(result, null);
    });

    test('returns null for empty string', function (assert) {
      const result = FullDateTransformation.hydrate('', null);
      assert.strictEqual(result, null);
    });

    test('converts a valid ISO string to a Luxon DateTime', function (assert) {
      const result = FullDateTransformation.hydrate('2024-06-15T10:30:00.000Z', null);
      assert.ok(result instanceof DateTime, 'result is a DateTime instance');
      assert.true(result.isValid, 'DateTime is valid');
      assert.strictEqual(result.year, 2024);
      assert.strictEqual(result.month, 6);
      assert.strictEqual(result.day, 15);
    });

    test('hydrated DateTime uses UTC zone', function (assert) {
      const result = FullDateTransformation.hydrate('2024-01-01T00:00:00.000Z', null);
      assert.strictEqual(result.zoneName, 'UTC');
    });

    test('converts a date-only ISO string', function (assert) {
      const result = FullDateTransformation.hydrate('2024-03-20', null);
      assert.ok(result instanceof DateTime);
      assert.true(result.isValid);
      assert.strictEqual(result.year, 2024);
      assert.strictEqual(result.month, 3);
      assert.strictEqual(result.day, 20);
    });
  });

  module('serialize', function () {
    test('returns null for null', function (assert) {
      const result = FullDateTransformation.serialize(null, null);
      assert.strictEqual(result, null);
    });

    test('serializes a native Date to ISO string', function (assert) {
      const date = new Date('2024-06-15T10:30:00.000Z');
      const result = FullDateTransformation.serialize(date, null);
      assert.strictEqual(result, '2024-06-15T10:30:00.000Z');
    });

    test('serializes a Luxon DateTime to ISO string', function (assert) {
      const dt = DateTime.fromISO('2024-06-15T10:30:00.000Z', { zone: 'utc' });
      const result = FullDateTransformation.serialize(dt, null);
      assert.strictEqual(typeof result, 'string');
      assert.ok(result.startsWith('2024-06-15'));
    });

    test('returns null for a Luxon DateTime that produces null toISO', function (assert) {
      const dt = DateTime.invalid('test reason');
      const result = FullDateTransformation.serialize(dt, null);
      assert.strictEqual(result, null);
    });
  });
});
