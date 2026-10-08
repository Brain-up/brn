import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Controller | description', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let controller = this.owner.lookup('controller:description');
    assert.ok(controller);
  });
});
