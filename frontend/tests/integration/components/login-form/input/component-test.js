import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | login-form/input', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    this.set('model', {});
    this.set('name', 'foo');
    await render(hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`);

    assert.dom('input').exists();
    assert.dom('label').exists();
  });
});
