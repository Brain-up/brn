import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Service | audio', function (hooks) {
  setupTest(hooks);

  test('registers objects', function (assert) {
    const testObject = {
      a: 1,
      testFunc() {
        assert.ok(true, "can call registered object's methods");
      },
    };
    let service = this.owner.lookup('service:audio');
    service.register(testObject);

    service.player.testFunc();
  });
});
