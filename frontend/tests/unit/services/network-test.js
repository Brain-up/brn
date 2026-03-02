import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { setupMSW } from '../../helpers/msw';

module('Unit | Service | network', function (hooks) {
  setupTest(hooks);
  setupMSW(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    const service = this.owner.lookup('service:network');
    assert.ok(service);
  });
});
