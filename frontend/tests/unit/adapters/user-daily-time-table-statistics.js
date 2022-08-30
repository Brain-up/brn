import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Adapter | user daily time table statistics', function (hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    let adapter = this.owner.lookup('adapter:user-daily-time-table-statistics');
    assert.ok(adapter);
  });
});
