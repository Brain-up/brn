import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import UiTabButton from 'brn/components/ui/tab-button';

module('Integration | Component | ui/tab-button', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><UiTabButton @title="foo"/></template>);

    assert.equal(this.element.textContent.trim(), 'foo');

    // Template block usage:
    await render(<template><UiTabButton @title="foo">
    template block text
    </UiTabButton></template>);

    assert.equal(this.element.textContent.trim(), 'template block text');
  });
});
