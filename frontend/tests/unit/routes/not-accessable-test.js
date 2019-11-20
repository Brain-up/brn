import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Route | notAccessable', function(hooks) {
  setupTest(hooks);

  test('it exists', function(assert) {
    let route = this.owner.lookup('route:not-accessable');
    assert.ok(route);
  });
});
