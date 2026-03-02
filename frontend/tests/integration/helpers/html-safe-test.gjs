import htmlSafe from 'brn/helpers/html-safe';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Helper | html-safe', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  // Replace this with your real tests.
  test('it renders', async function (assert) {
    this.set('inputValue', '1234');

    const self = this;




    await render(<template>{{htmlSafe self.inputValue}}</template>);

    assert.equal(this.element.textContent.trim(), '1234');
  });
});
