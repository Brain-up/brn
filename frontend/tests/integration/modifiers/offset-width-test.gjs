import offsetWidth from 'brn/modifiers/offset-width';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Modifier | offset-width', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  // Replace this with your real tests.
  test('it renders', async function (assert) {
    await render(<template><div {{offsetWidth}}></div></template>);

    assert.ok(true);
  });
});
