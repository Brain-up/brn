import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';

module('Unit | Route | profile/index', function (hooks) {
  setupTest(hooks);

  test('model calls network.loadCurrentUser', async function (assert) {
    assert.expect(1);

    class MockNetwork extends Service {
      loadCurrentUser() {
        assert.ok(true, 'loadCurrentUser was called');
        return Promise.resolve();
      }
    }
    this.owner.register('service:network', MockNetwork);

    const route = this.owner.lookup('route:profile/index');
    await route.model();
  });

  test('model awaits loadCurrentUser before resolving', async function (assert) {
    const callOrder = [];

    class MockNetwork extends Service {
      async loadCurrentUser() {
        callOrder.push('loadCurrentUser:start');
        await new Promise((r) => setTimeout(r, 10));
        callOrder.push('loadCurrentUser:end');
      }
    }
    this.owner.register('service:network', MockNetwork);

    const route = this.owner.lookup('route:profile/index');
    await route.model();
    callOrder.push('model:resolved');

    assert.deepEqual(
      callOrder,
      ['loadCurrentUser:start', 'loadCurrentUser:end', 'model:resolved'],
      'model waits for loadCurrentUser to complete before resolving',
    );
  });
});
