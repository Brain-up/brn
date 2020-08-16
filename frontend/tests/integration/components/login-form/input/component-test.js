import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, typeIn } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import { tracked } from '@glimmer/tracking';

function getNumSymbols(num) {
  num = num - 1;
  return new Array(num).fill('A').join('');
}

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

  test('it renders without label', async function(assert) {
    this.set('model', {});
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" />`,
    );

    assert.dom('input').exists();
    assert.dom('label').doesNotExist();
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
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );

    await typeIn('input', getNumSymbols(50));
    assert.equal(this.model.foo, getNumSymbols(50));
  });

  test('if the field is filled and the length of the field > 50 show warning', async function(assert) {
    class Model {
      @tracked
      foo = '';
    }
    this.set('model', new Model());
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );

    await typeIn('input', getNumSymbols(51));
    assert.dom('[data-test-warning-message]').exists();
  });

  test('if the field is filled and the length of the field = 50 show warning', async function(assert) {
    class Model {
      @tracked
      foo = '';
    }
    this.set('model', new Model());
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );

    await typeIn('input', getNumSymbols(50));
    assert.dom('[data-test-warning-message]').exists();
  });

  test('if the field is filled and the length of the field < 50 not show', async function(assert) {
    class Model {
      @tracked
      foo = '';
    }
    this.set('model', new Model());
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );

    assert.dom('[data-test-warning-message]').doesNotExist();
  });

  test('if the field was filled and the length of the field > 50 show warning', async function(assert) {
    this.set('model', { foo: getNumSymbols(51) });
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );

    assert.dom('[data-test-warning-message]').exists();
  });

  test('if the field was filled and the length of the field = 50 show warning', async function(assert) {
    this.set('model', { foo: getNumSymbols(50) });
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );

    assert.dom('[data-test-warning-message]').exists();
  });

  test('if the field was filled and the length of the field < 50 not show', async function(assert) {
    this.set('model', { foo: getNumSymbols(49) });
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );

    assert.dom('[data-test-warning-message]').doesNotExist();
  });

  test('if the attribute warning`s exists show warning', async function(assert) {
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @name="warning" @warning="Foo" @type="text" @label="Foo" />`,
    );

    assert.dom('[data-test-warning-message="warning"]').hasText('Foo');
  });
});
