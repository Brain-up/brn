import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Schema | headphone', function (hooks) {
  setupTest(hooks);

  test('creates a headphone record with attributes', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('headphone', {
      description: 'Over-ear headphones',
      name: 'Sony WH-1000XM4',
      active: true,
      type: 'OVER_EAR',
    });
    assert.ok(record, 'record is created');
    assert.strictEqual(record.description, 'Over-ear headphones');
    assert.strictEqual(record.name, 'Sony WH-1000XM4');
    assert.strictEqual(record.active, true);
    assert.strictEqual(record.type, 'OVER_EAR');
  });

  test('creates a headphone record with default undefined attributes', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('headphone', {});
    assert.ok(record, 'record is created');
    assert.strictEqual(record.description, undefined);
    assert.strictEqual(record.name, undefined);
    assert.strictEqual(record.active, undefined);
  });

  test('headphone active can be set to false', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('headphone', {
      active: false,
    });
    assert.strictEqual(record.active, false);
  });
});
