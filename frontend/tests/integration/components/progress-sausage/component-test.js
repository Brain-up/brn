import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | progress-sausage', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    this.set('items', [
      { completedInCurrentCycle: true },
      { completedInCurrentCycle: false },
    ]);

    await render(hbs`<ProgressSausage @progressItems={{this.items}} />`);

    assert.dom('[data-test-progress-sausage]').exists();
    assert
      .dom('[data-test-progress-sausage]')
      .hasAttribute('style', 'width:50%;');

    this.set('items', [
      { completedInCurrentCycle: false },
      { completedInCurrentCycle: false },
    ]);

    assert
      .dom('[data-test-progress-sausage]')
      .hasAttribute('style', 'width:0%;');

    this.set('items', [
      { completedInCurrentCycle: true },
      { completedInCurrentCycle: true },
    ]);

    assert
      .dom('[data-test-progress-sausage]')
      .hasAttribute('style', 'width:100%;');
  });
});
