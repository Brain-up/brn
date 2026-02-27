import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Helper | subtract', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it subtracts the second argument from the first', async function (assert) {
    this.set('minuend', 10);
    this.set('subtrahend', 2);

    await render(hbs`{{subtract this.minuend this.subtrahend}}`);

    assert.equal(this.element.textContent.trim(), '8');
  });
});
