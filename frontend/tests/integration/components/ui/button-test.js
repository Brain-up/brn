import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | ui/button', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<Ui::Button @title="foo"/>`);

    assert.equal(this.element.textContent.trim(), 'foo');

    // Template block usage:
    await render(hbs`
      <Ui::Button @title="foo">
        template block text
      </Ui::Button>
    `);

    assert.equal(this.element.textContent.trim(), 'template block text');
  });
});
