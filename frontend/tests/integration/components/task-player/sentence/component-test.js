import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';

module('Integration | Component | task-player/sentence', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    assert.equal(this.element.textContent.trim(), '');
  });
});
