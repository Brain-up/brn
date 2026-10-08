import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Schema | signal', function (hooks) {
  setupTest(hooks);

  test('creates a signal record with attributes', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('signal', {
      frequency: 1000,
      duration: 500,
      length: 300,
    });
    assert.ok(record, 'record is created');
    assert.strictEqual(record.frequency, 1000);
    assert.strictEqual(record.duration, 500);
    assert.strictEqual(record.length, 300);
  });

  test('creates a signal record with default undefined attributes', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('signal', {});
    assert.ok(record, 'record is created');
    assert.strictEqual(record.frequency, undefined);
    assert.strictEqual(record.duration, undefined);
    assert.strictEqual(record.length, undefined);
  });
});
