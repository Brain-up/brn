import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import UiStatsIcon from 'brn/components/ui/stats/icon';

module('Integration | Component | ui/stats/icon', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><UiStatsIcon class="foo" /></template>);

    assert.dom('svg').exists();
    assert.dom('svg').hasClass('foo');
  });
});
