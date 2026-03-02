import sum from 'brn/helpers/sum';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Helper | sum', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  // Replace this with your real tests.
  test('it renders', async function (assert) {
    await render(<template>{{sum 15 32}}</template>);

    assert.equal(this.element.textContent.trim(), '47');
  });
});
