import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import UiButton from 'brn/components/ui/button';

module('Integration | Component | ui/button', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><UiButton @title="foo"/></template>);

    assert.equal(this.element.textContent.trim(), 'foo');

    // Template block usage:
    await render(<template><UiButton @title="foo">
    template block text
    </UiButton></template>);

    assert.equal(this.element.textContent.trim(), 'template block text');
  });
});
