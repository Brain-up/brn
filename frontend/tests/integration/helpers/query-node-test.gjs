import { get } from '@ember/helper';
import queryNode from 'brn/helpers/query-node';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Helper | query-node', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  // Replace this with your real tests.
  test('it renders', async function (assert) {
    await render(<template>{{get (queryNode "body") "textContent"}}</template>);

    assert.ok(this.element.textContent.trim());
  });
});
