import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Service | study-config', function(hooks) {
  setupTest(hooks);

  // TODO: Replace this with your real tests.
  test('it exists', function(assert) {
    let service = this.owner.lookup('service:study-config');
    assert.ok(service);
  });

  test('it toggles image visibility', function(assert) {
    let service = this.owner.lookup('service:study-config');
    assert.true(service.showImages);
    service.toggleImageVisibility();
    assert.false(service.showImages);
    service.toggleImageVisibility();
    assert.true(service.showImages);
  });
});
