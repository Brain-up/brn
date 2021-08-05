import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Transform | full-date', function (hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    let transform = this.owner.lookup('transform:full-date');
    assert.ok(transform);
  });

  test('can serialize', function (assert) {
    const transform = this.owner.lookup('transform:full-date');
    const date = new Date(1984, 0, 24);
    const dateTransformed = transform.serialize(date);
    assert.equal(dateTransformed, date.toISOString());
  });

  test('can serialize null date', function (assert) {
    const transform = this.owner.lookup('transform:full-date');
    const dateTransformed = transform.serialize(null);
    assert.equal(dateTransformed, null);
  });

  test('can deserialize', function (assert) {
    const transform = this.owner.lookup('transform:full-date');
    const date = '2021-01';
    const dateTransformed = transform.deserialize(date);
    assert.equal(dateTransformed.toFormat('yyyy-MM-dd'), '2021-01-01');
  });

  test('can deserialize null date', function (assert) {
    const transform = this.owner.lookup('transform:full-date');
    const dateTransformed = transform.deserialize(null);
    assert.equal(dateTransformed, null);
  });
});
