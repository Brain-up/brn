import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, typeIn } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | login-form/input', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    this.set('model', {});
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );

    assert.dom('input').exists();
    assert.dom('label').exists();
  });

  test('it able to show error on empty fields', async function(assert) {
    this.set('model', { foo: '' });
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );
    assert.dom('.border-red-500').exists();
  });

  test('it trim input values', async function(assert) {
    this.set('model', { foo: '' });
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );
    await typeIn('input', ' hello ');
    assert.equal(this.model.foo, 'hello');
  });

  test('it can accept only 50 symbols', async function(assert) {
    this.set('model', { foo: '' });
    this.set('name', 'foo');
    let longText = new Array(49).fill('A').join('');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );
    await typeIn('input', longText + longText);
    assert.equal(this.model.foo, longText);
  });
});
