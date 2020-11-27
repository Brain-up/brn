import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Helper | model-for-route', function(hooks) {
  setupRenderingTest(hooks);

  // TODO: Replace this with your real tests.
  test('it renders', async function(assert) {

    await render(hbs`{{model-for-route 'foo-bar'}}`);

    assert.equal(this.element.textContent.trim(), '');
  });
});
