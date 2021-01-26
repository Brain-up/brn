import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | ui/tab-button', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<Ui::Tab-Button @title="foo"/>`);

    assert.equal(this.element.textContent.trim(), 'foo');

    // Template block usage:
    await render(hbs`
      <Ui::Tab-Button @title="foo">
        template block text
      </Ui::Tab-Button>
    `);

    assert.equal(this.element.textContent.trim(), 'template block text');
  });
});