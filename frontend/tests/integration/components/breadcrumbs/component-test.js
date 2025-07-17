import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import { setupMirage } from "ember-cli-mirage/test-support";

module('Integration | Component | breadcrumbs', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');
  setupMirage(hooks);

  test('it shows group link', async function (assert) {
    await render(hbs`<Breadcrumbs />`);
    assert.dom('a').exists();
  });
});
