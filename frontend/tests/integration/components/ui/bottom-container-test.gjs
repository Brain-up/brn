import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import UiBottomContainer from 'brn/components/ui/bottom-container';

module('Integration | Component | ui/bottom-container', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><UiBottomContainer /></template>);

    assert.equal(this.element.textContent.trim(), '');

    // Template block usage:
    await render(<template><UiBottomContainer>
    template block text
    </UiBottomContainer></template>);

    assert.equal(this.element.textContent.trim(), 'template block text');
  });
});
