import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Serializer | task/single simple words', function (hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    let store = this.owner.lookup('service:store');
    let serializer = store.serializerFor('task/single-simple-words');

    assert.ok(serializer);
  });

  test('it serializes records', function (assert) {
    let store = this.owner.lookup('service:store');
    let record = store.createRecord('task/single-simple-words', {});

    let serializedRecord = record.serialize();

    assert.ok(serializedRecord);
  });
});
