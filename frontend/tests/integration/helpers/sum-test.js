import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Helper | sum', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  // Replace this with your real tests.
  test('it renders', async function (assert) {
    await render(hbs`{{sum 15 32}}`);

    assert.equal(this.element.textContent.trim(), '47');
  });
});
