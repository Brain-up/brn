import { array } from '@ember/helper';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import SubgroupNavigation from 'brn/components/subgroup-navigation';

module('Integration | Component | subgroup-navigation', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><SubgroupNavigation @group={{(array)}} /></template>);

    assert.dom('ul').exists();
  });
});
