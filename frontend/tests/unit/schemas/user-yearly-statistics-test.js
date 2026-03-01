import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { UserYearlyStatisticsExtension } from 'brn/schemas/user-yearly-statistics';
import { DateTime } from 'luxon';

module('Unit | Schema | user-yearly-statistics', function (hooks) {
  setupTest(hooks);

  test('creates a user-yearly-statistics record', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('user-yearly-statistics', {
      exercisingTimeSeconds: 7200,
      progress: 'GREAT',
      exercisingDays: 15,
    });
    assert.ok(record, 'record is created');
    assert.strictEqual(record.exercisingTimeSeconds, 7200);
    assert.strictEqual(record.progress, 'GREAT');
    assert.strictEqual(record.exercisingDays, 15);
  });

  module('extension getters (tested directly)', function () {
    test('time formats seconds as h:m:s', function (assert) {
      const context = { exercisingTimeSeconds: 7323 };
      const timeGetter = Object.getOwnPropertyDescriptor(
        UserYearlyStatisticsExtension.features,
        'time',
      ).get;
      const result = timeGetter.call(context);
      // 7323 seconds = 2 hours, 2 minutes, 3 seconds
      assert.strictEqual(result, '02:02:03');
    });

    test('month returns the month name from date', function (assert) {
      const dt = DateTime.fromISO('2024-12-01', { zone: 'utc', locale: 'en' });
      const context = { date: dt };
      const monthGetter = Object.getOwnPropertyDescriptor(
        UserYearlyStatisticsExtension.features,
        'month',
      ).get;
      const result = monthGetter.call(context);
      assert.strictEqual(result, 'December');
    });

    test('year returns the year from date', function (assert) {
      const dt = DateTime.fromISO('2025-03-20', { zone: 'utc' });
      const context = { date: dt };
      const yearGetter = Object.getOwnPropertyDescriptor(
        UserYearlyStatisticsExtension.features,
        'year',
      ).get;
      const result = yearGetter.call(context);
      assert.strictEqual(result, 2025);
    });

    test('days returns exercisingDays', function (assert) {
      const context = { exercisingDays: 42 };
      const daysGetter = Object.getOwnPropertyDescriptor(
        UserYearlyStatisticsExtension.features,
        'days',
      ).get;
      const result = daysGetter.call(context);
      assert.strictEqual(result, 42);
    });

    test('days returns 0 when exercisingDays is 0', function (assert) {
      const context = { exercisingDays: 0 };
      const daysGetter = Object.getOwnPropertyDescriptor(
        UserYearlyStatisticsExtension.features,
        'days',
      ).get;
      const result = daysGetter.call(context);
      assert.strictEqual(result, 0);
    });
  });
});
