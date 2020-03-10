import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | registration-form', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<RegistrationForm />`);

    assert.dom('form').exists();
    assert.dom('[data-test-submit-form]').hasTagName('button');
    assert.dom('[name="lastName"]').hasAttribute('required');
    assert.dom('[name="firstName"]').hasAttribute('required');
    assert.dom('[data-test-form-warning]').doesNotExist();
  });
});
