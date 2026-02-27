import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | footer', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    await render(hbs`<Footer />`);

    assert.dom('[data-test-support-logo]').exists({ count: 5 });
    assert.dom('[data-test-support-message]').exists();
  });
});
