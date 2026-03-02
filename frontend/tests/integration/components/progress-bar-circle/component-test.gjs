import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Component | progress-bar-circle', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><ProgressBarCircle /></template>);

    assert.ok(this.element.textContent.trim(), '');

    // Template block usage:
    await render(<template><ProgressBarCircle>
    template block text
    </ProgressBarCircle></template>);

    assert.ok(this.element.textContent.trim(), 'template block text');
  });
});
