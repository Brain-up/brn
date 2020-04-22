import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, fillIn, click } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import Service from '@ember/service';

module('Integration | Component | login-form', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<LoginForm />`);

    assert.dom('form').exists();
    assert.dom('[data-test-submit-form]').hasTagName('button');
    assert.dom('[name="login"]').hasAttribute('required');
    assert.dom('[name="password"]').hasAttribute('required');
    assert.dom('[data-test-form-warning]').doesNotExist();
  });

  test('it showing warning on empty fields if edited', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<LoginForm />`);
    assert.dom('[data-test-form-warning]').doesNotExist();

    await fillIn('[name="login"]', 'a');
    await fillIn('[name="password"]', 'b');
    assert.dom('[data-test-form-warning]').exists();

    await fillIn('[name="login"]', '');
    await fillIn('[name="password"]', 'b');
    assert.dom('[data-test-form-warning]').exists();

    await fillIn('[name="login"]', 'a');
    await fillIn('[name="password"]', '');
    assert.dom('[data-test-form-warning]').exists();

    await fillIn('[name="login"]', '');
    await fillIn('[name="password"]', '');
    assert.dom('[data-test-form-warning]').exists();
  });

  test('it requires @ sign in email', async function(assert) {
    await render(hbs`<LoginForm />`);
    await fillIn('[name="login"]', 'a@b');
    await fillIn('[name="password"]', 'b');
    assert.dom('[data-test-form-warning]').doesNotExist();
  });

  test('login button works as expected', async function(assert) {
    assert.expect(3);
    class MockSession extends Service {
      authenticate(type, login, password) {
        assert.ok(type);
        assert.ok(login);
        assert.ok(password);
      }
    }
    this.owner.register('service:session', MockSession);

    await render(hbs`<LoginForm />`);

    await fillIn('[name="login"]', 'a@b');
    await fillIn('[name="password"]', 'b');
    await click('[data-test-submit-form]');
  });

  test('incorrect login has feedback #1', async function(assert) {
    class MockSession extends Service {
      authenticate() {
        return Promise.reject({
          responseJSON: {
            errors: ['foo'],
          },
        });
      }
    }
    this.owner.register('service:session', MockSession);
    await render(hbs`<LoginForm />`);

    await fillIn('[name="login"]', 'a@b');
    await fillIn('[name="password"]', 'b');
    await click('[data-test-submit-form]');
    assert.dom('[data-test-form-error]').hasText('foo');
  });

  test('incorrect login has feedback #2', async function(assert) {
    class MockSession extends Service {
      authenticate() {
        return Promise.reject({
          error: 'boo',
        });
      }
    }
    this.owner.register('service:session', MockSession);
    await render(hbs`<LoginForm />`);

    await fillIn('[name="login"]', 'a@b');
    await fillIn('[name="password"]', 'b');
    await click('[data-test-submit-form]');
    assert.dom('[data-test-form-error]').hasText('boo');
  });

  test('incorrect login has feedback #3', async function(assert) {
    class MockSession extends Service {
      authenticate() {
        return Promise.reject('zoo');
      }
    }
    this.owner.register('service:session', MockSession);
    await render(hbs`<LoginForm />`);

    await fillIn('[name="login"]', 'a@b');
    await fillIn('[name="password"]', 'b');
    await click('[data-test-submit-form]');
    assert.dom('[data-test-form-error]').hasText('zoo');
  });

  test('incorrect login has feedback with mapped local errors', async function(assert) {
    class MockSession extends Service {
      authenticate() {
        return Promise.reject('Bad credentials');
      }
    }

    this.owner.register('service:session', MockSession);
    await render(hbs`<LoginForm />`);

    await fillIn('[name="login"]', 'a@b');
    await fillIn('[name="password"]', 'b');
    await click('[data-test-submit-form]');
    assert
      .dom('[data-test-form-error]')
      .hasText('Неправильный логин или пароль.');
  });

  test('incorrect form values does not invoke form submit', async function(assert) {
    let isSubmitCalled = false;
    class MockSession extends Service {
      authenticate() {
        isSubmitCalled = true;
      }
    }

    this.owner.register('service:session', MockSession);
    await render(hbs`<LoginForm />`);

    await fillIn('[name="login"]', 'a');
    await fillIn('[name="password"]', 'b');
    await click('[data-test-submit-form]');
    assert.equal(isSubmitCalled, false);
  });
});
