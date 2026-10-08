import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Schema | task/signal', function (hooks) {
  setupTest(hooks);

  test('creates a task/signal record', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/signal', {
      name: 'Signal Task',
      order: 1,
      exerciseType: 'FREQUENCY_SIGNALS',
    });
    assert.ok(record, 'record is created');
    assert.strictEqual(record.name, 'Signal Task');
    assert.strictEqual(record.order, 1);
    assert.strictEqual(record.exerciseType, 'FREQUENCY_SIGNALS');
  });

  test('task/signal has base task fields', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/signal', {
      name: 'Test',
      active: true,
      repetitionCount: 3,
      shouldBeWithPictures: false,
    });
    assert.strictEqual(record.name, 'Test');
    assert.strictEqual(record.active, true);
    assert.strictEqual(record.repetitionCount, 3);
    assert.strictEqual(record.shouldBeWithPictures, false);
  });

  test('task/signal has local fields with defaults', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/signal', {});
    assert.strictEqual(record.isManuallyCompleted, false);
    assert.strictEqual(record.nextAttempt, false);
    assert.strictEqual(record.available, false);
  });
});
