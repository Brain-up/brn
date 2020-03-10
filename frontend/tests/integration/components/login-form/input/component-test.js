import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | login-form/input', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {

    this.set('model', {});
    this.set('name', 'foo');
    await render(hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`);

    assert.dom('input').exists();
    assert.dom('label').exists();
  });

  test('it able to show error on empty fields', async function(assert) {

    this.set('model', {foo:''});
    this.set('name', 'foo');
    await render(hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`);
    assert.dom('.border-red-500').exists();
  });
});
