import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Helper | style-namespace', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  // Replace this with your real tests.
  test('it renders', async function (assert) {
    this.set('inputValue', 'task-player');

    const self = this;




    await render(<template>{{style-namespace self.inputValue}}</template>);

    assert.dom().includesText('task-player');
  });
});
