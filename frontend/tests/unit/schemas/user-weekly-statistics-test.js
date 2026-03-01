import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { UserWeeklyStatisticsExtension } from 'brn/schemas/user-weekly-statistics';
import { DateTime } from 'luxon';

module('Unit | Schema | user-weekly-statistics', function (hooks) {
  setupTest(hooks);

  test('creates a user-weekly-statistics record', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('user-weekly-statistics', {
      exercisingTimeSeconds: 3661,
      progress: 'GOOD',
    });
    assert.ok(record, 'record is created');
    assert.strictEqual(record.exercisingTimeSeconds, 3661);
    assert.strictEqual(record.progress, 'GOOD');
  });

  module('extension getters (tested directly)', function () {
    test('time formats seconds as h:m:s', function (assert) {
      const context = { exercisingTimeSeconds: 3661 };
      const timeGetter = Object.getOwnPropertyDescriptor(
        UserWeeklyStatisticsExtension.features,
        'time',
      ).get;
      const result = timeGetter.call(context);
      // 3661 seconds = 1 hour, 1 minute, 1 second
      assert.strictEqual(result, '01:01:01');
    });

    test('time formats zero seconds', function (assert) {
      const context = { exercisingTimeSeconds: 0 };
      const timeGetter = Object.getOwnPropertyDescriptor(
        UserWeeklyStatisticsExtension.features,
        'time',
      ).get;
      const result = timeGetter.call(context);
      assert.strictEqual(result, '00:00:00');
    });

    test('month returns the month name from date', function (assert) {
      const dt = DateTime.fromISO('2024-06-15', { zone: 'utc', locale: 'en' });
      const context = { date: dt };
      const monthGetter = Object.getOwnPropertyDescriptor(
        UserWeeklyStatisticsExtension.features,
        'month',
      ).get;
      const result = monthGetter.call(context);
      assert.strictEqual(result, 'June');
    });

    test('year returns the year from date', function (assert) {
      const dt = DateTime.fromISO('2024-06-15', { zone: 'utc' });
      const context = { date: dt };
      const yearGetter = Object.getOwnPropertyDescriptor(
        UserWeeklyStatisticsExtension.features,
        'year',
      ).get;
      const result = yearGetter.call(context);
      assert.strictEqual(result, 2024);
    });
  });
});
