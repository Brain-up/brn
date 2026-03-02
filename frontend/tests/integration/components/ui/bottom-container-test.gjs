import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Component | ui/bottom-container', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><Ui::BottomContainer /></template>);

    assert.equal(this.element.textContent.trim(), '');

    // Template block usage:
    await render(<template><Ui::BottomContainer>
    template block text
    </Ui::BottomContainer></template>);

    assert.equal(this.element.textContent.trim(), 'template block text');
  });
});
