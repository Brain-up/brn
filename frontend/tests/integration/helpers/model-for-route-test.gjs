import modelForRoute from 'brn/helpers/model-for-route';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Helper | model-for-route', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  // TODO: Replace this with your real tests.
  test('it renders', async function (assert) {
    await render(<template>{{modelForRoute 'foo-bar'}}</template>);

    assert.equal(this.element.textContent.trim(), '');
  });
});
