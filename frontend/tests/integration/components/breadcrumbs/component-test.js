import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';

module('Integration | Component | breadcrumbs', function (hooks) {
  setupRenderingTest(hooks);
  setupMirage(hooks);

  test('it shows group link', async function (assert) {
    await render(hbs`<Breadcrumbs />`);
    assert.dom('a').exists();
  });
});
