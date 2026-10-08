import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Schema | group', function (hooks) {
  setupTest(hooks);

  test('creates a group record', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {
      name: 'Test Group',
      description: 'A test group',
      locale: 'en-us',
      order: 1,
    });
    assert.ok(model, 'record is created');
    assert.strictEqual(model.name, 'Test Group');
    assert.strictEqual(model.locale, 'en-us');
    assert.strictEqual(model.order, 1);
  });

  test('parent is always null (top-level entity)', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    assert.strictEqual(model.parent, null);
  });

  test('sortChildrenBy returns id', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    assert.strictEqual(model.sortChildrenBy, 'id');
  });

  test('allSiblings is empty (groups have no parent)', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    assert.deepEqual(model.allSiblings, []);
  });

  test('isFirst is true (groups always first)', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    assert.true(model.isFirst);
  });

  test('canInteract is true (groups always interactable)', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    assert.true(model.canInteract);
  });

  test('previousSiblings is empty', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    assert.deepEqual(model.previousSiblings, []);
  });

  test('nextSiblings is empty', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    assert.deepEqual(model.nextSiblings, []);
  });

  test('isCompleted is false with no tasks manager data', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    assert.false(model.isCompleted);
  });

  test('isCompleted is true when manually completed', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    model.isManuallyCompleted = true;
    assert.true(model.isCompleted);
  });

  test('isQueryParams returns undefined', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('group', {});
    assert.strictEqual(model.isQueryParams, undefined);
  });
});
