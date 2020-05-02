import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, typeIn, fillIn } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import { tracked } from '@glimmer/tracking';

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

  test('if the field is filled and the length of the field > 50 show warning', async function(assert) {
    class Model {
      @tracked
      foo = '';
    }
    this.set('model', new Model());
    this.set('name', 'foo');
    let longText = new Array(50).fill('A').join('');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );
    await typeIn('input', longText);
    assert.dom('[data-test-warning-message]').exists();
  });

  test('if the field is filled and the length of the field = 50 show warning', async function(assert) {
    class Model {
      @tracked
      foo = '';
    }
    this.set('model', new Model());
    this.set('name', 'foo');
    let longText = new Array(49).fill('A').join('');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );
    await typeIn('input', longText);
    assert.dom('[data-warning-form-warning]').doesNotExist();
  });

  test('if the field is filled and the length of the field < 50 not show', async function(assert) {
    class Model {
      @tracked
      foo = '';
    }
    this.set('model', new Model());
    this.set('name', 'foo');
    let longText = new Array(48).fill('A').join('');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );
    await fillIn('input', longText + longText);
    assert.dom('[data-test-warning-message]').exists();
  });

  test('if the field was filled and the length of the field > 50 show warning', async function(assert) {
    let longText = new Array(50).fill('A').join('');
    this.set('model', { foo: longText });
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );
    await this.pauseTest();
    assert.dom('[data-test-warning-message]').exists();
  });

  test('if the field was filled and the length of the field = 50 show warning', async function(assert) {
    let longText = new Array(49).fill('A').join('');
    this.set('model', { foo: longText });
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );
    await fillIn('input', longText);
    assert.dom('[data-test-warning-message]').exists();
  });

  test('if the field was filled and the length of the field < 50 not show', async function(assert) {
    let longText = new Array(48).fill('A').join('');
    this.set('model', { foo: longText });
    this.set('name', 'foo');
    await render(
      hbs`{{!-- @ts-nocheck --}}<LoginForm::Input @model={{this.model}} @name={{this.name}} @type="text" @label="Foo" />`,
    );
    await fillIn('input', longText);
    assert.dom('[data-test-warning-message]').doesNotExist();
  });
});
