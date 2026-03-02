import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import UiTaskContent from 'brn/components/ui/task-content';

module('Integration | Component | ui/task-content', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><UiTaskContent /></template>);

    assert.equal(this.element.textContent.trim(), '');

    // Template block usage:
    await render(<template><UiTaskContent>
    template block text
    </UiTaskContent></template>);

    assert.equal(this.element.textContent.trim(), 'template block text');
  });
});
