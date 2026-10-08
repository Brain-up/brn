import subtract from 'brn/helpers/subtract';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Helper | subtract', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it subtracts the second argument from the first', async function (assert) {
    this.set('minuend', 10);
    this.set('subtrahend', 2);

    const self = this;




    await render(<template>{{subtract self.minuend self.subtrahend}}</template>);

    assert.equal(this.element.textContent.trim(), '8');
  });
});
