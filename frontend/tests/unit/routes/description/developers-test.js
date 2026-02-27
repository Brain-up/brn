import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Route | description/developers', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let route = this.owner.lookup('route:description/developers');
    assert.ok(route);
  });
});
