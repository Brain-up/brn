import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Route | series/exercise/task', function(hooks) {
  setupTest(hooks);

  test('it exists', function(assert) {
    let route = this.owner.lookup('route:group/series/exercise/task');
    assert.ok(route);
  });
});
