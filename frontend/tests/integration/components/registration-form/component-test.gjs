import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, fillIn, click } from '@ember/test-helpers';
import Service from '@ember/service';
import RegistrationForm from 'brn/components/registration-form';

function getDate(num) {
  let date = new Date();
  return date.getFullYear() + num;
}

module('Integration | Component | registration-form', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    await render(<template><RegistrationForm /></template>);

    assert.dom('form').exists();
    assert.dom('[data-test-submit-form]').hasTagName('button');
    assert.dom('[name="firstName"]').hasAttribute('required');
    assert.dom('[data-test-form-warning]').doesNotExist();
  });

  test('it send register request if all fields filled', async function (assert) {
    assert.expect(4);

    class MockFirebaseAuthenticator {
      registerUser() {
        return Promise.resolve();
      }
    }

    class MockNetwork extends Service {
      loadCurrentUser() {
        return Promise.resolve();
      }
      patchUserInfo(fields) {
        assert.ok(fields, 'patchUserInfo called with user fields');
        return {
          ok: true,
        };
      }
    }

    class MockSession extends Service {
      isAuthenticated = false;
      authenticate(type, login, password) {
        assert.ok(type, 'authenticate called with type');
        assert.ok(login, 'authenticate called with login');
        assert.ok(password, 'authenticate called with password');
        return Promise.resolve();
      }
    }

    this.owner.register('authenticator:firebase', MockFirebaseAuthenticator);
    this.owner.register('service:session', MockSession);
    this.owner.register('service:network', MockNetwork);

    await render(<template><RegistrationForm /></template>);
    await fillIn('[name="firstName"]', 'b');
    await fillIn('[name="email"]', 'c@name.com');
    await fillIn('[name="password"]', 'd');
    await fillIn('[name="repeatPassword"]', 'd');
    await fillIn('[name="birthday"]', '1991');
    await click('[name="agreement"]');
    await click('[id="male"]');
    await click('[data-test-submit-form]');
  });

  test('it able to handle registration error', async function (assert) {
    assert.expect(2);

    class MockFirebaseAuthenticator {
      registerUser() {
        assert.ok(true, 'registerUser was called');
        return Promise.reject(new Error('foo'));
      }
    }

    class MockNetwork extends Service {
      loadCurrentUser() {
        return Promise.resolve();
      }
      patchUserInfo() {
        return { ok: true };
      }
    }

    class MockSession extends Service {
      isAuthenticated = false;
      authenticate() {
        return Promise.resolve();
      }
    }

    this.owner.register('authenticator:firebase', MockFirebaseAuthenticator);
    this.owner.register('service:session', MockSession);
    this.owner.register('service:network', MockNetwork);

    await render(<template><RegistrationForm /></template>);
    await fillIn('[name="firstName"]', 'b');
    await fillIn('[name="email"]', 'c@name.com');
    await fillIn('[name="password"]', 'd');
    await fillIn('[name="repeatPassword"]', 'd');
    await fillIn('[name="birthday"]', '1991');
    await click('[name="agreement"]');
    await click('[id="male"]');
    await click('[data-test-submit-form]');
    assert.dom('[data-test-form-error]').hasText('foo');
  });

  test('show message when entering date below acceptable', async function (assert) {
    await render(<template><RegistrationForm /></template>);

    await fillIn('input[name="birthday"]', '1911');

    assert.dom('[data-test-warning-message="birthday"]').exists();
  });

  test('show message when entering date higher than allowed', async function (assert) {
    await render(<template><RegistrationForm /></template>);

    await fillIn('input[name="birthday"]', getDate(1));

    assert.dom('[data-test-warning-message="birthday"]').exists();
  });

  test('do not show message when entering valid date', async function (assert) {
    await render(<template><RegistrationForm /></template>);

    await fillIn('input[name="birthday"]', getDate(0));

    assert.dom('[data-test-warning-message="birthday"]').doesNotExist();
  });
});
