import scrollTop from 'brn/modifiers/scroll-top';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Modifier | scroll-top', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  // Replace this with your real tests.
  test('it renders', async function (assert) {
    await render(<template><div {{scrollTop}}></div></template>);

    assert.ok(true);
  });
});
