import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, fillIn, click } from '@ember/test-helpers';
import Service from '@ember/service';
import EmberObject from '@ember/object';
import RegistrationForm from 'brn/components/registration-form';

function getDate(num) {
  let date = new Date();
  return date.getFullYear() + num;
}

async function fillAndSubmit() {
  await fillIn('[name="firstName"]', 'b');
  await fillIn('[name="email"]', 'c@name.com');
  await fillIn('[name="password"]', 'Test1234');
  await fillIn('[name="repeatPassword"]', 'Test1234');
  await fillIn('[name="birthday"]', '1991');
  await click('[name="agreement"]');
  await click('[id="male"]');
  await click('[data-test-submit-form]');
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
    // eslint-disable-next-line ember/no-classic-classes
    const MockFirebaseAuthenticator = EmberObject.extend({
      registerUser() {
        return Promise.resolve();
      },
    });

    class MockNetwork extends Service {
      loadCurrentUser() {
        assert.step('loadCurrentUser');
        return Promise.resolve();
      }
      loadCloudUrl() {
        return Promise.resolve();
      }
      patchUserInfo(fields) {
        assert.step('patchUserInfo');
        assert.ok(fields, 'patchUserInfo called with user fields');
        return Promise.resolve(fields);
      }
    }

    class MockSession extends Service {
      isAuthenticated = false;
      authenticate() {
        assert.step('authenticate');
        return Promise.resolve();
      }
    }

    this.owner.register('authenticator:firebase', MockFirebaseAuthenticator);
    this.owner.register('service:session', MockSession);
    this.owner.register('service:network', MockNetwork);

    await render(<template><RegistrationForm /></template>);
    await fillAndSubmit();

    // Login loads the user, then the profile is patched and the user reloaded.
    assert.verifySteps([
      'authenticate',
      'loadCurrentUser',
      'patchUserInfo',
      'loadCurrentUser',
    ]);
  });

  test('redirects to index only after the profile is patched and reloaded', async function (assert) {
    // eslint-disable-next-line ember/no-classic-classes
    const MockFirebaseAuthenticator = EmberObject.extend({
      registerUser() {
        return Promise.resolve();
      },
    });

    class MockNetwork extends Service {
      loadCurrentUser() {
        assert.step('loadCurrentUser');
        return Promise.resolve();
      }
      loadCloudUrl() {
        return Promise.resolve();
      }
      patchUserInfo(fields) {
        assert.step('patchUserInfo');
        return Promise.resolve(fields);
      }
    }

    class MockSession extends Service {
      isAuthenticated = false;
      authenticate() {
        // Mirror production: the session becomes authenticated after login.
        this.isAuthenticated = true;
        return Promise.resolve();
      }
    }

    this.owner.register('authenticator:firebase', MockFirebaseAuthenticator);
    this.owner.register('service:session', MockSession);
    this.owner.register('service:network', MockNetwork);

    // Spy on the real router's transitionTo so the template's <LinkTo>s keep
    // rendering while we record when the redirect happens.
    this.owner.lookup('service:router').transitionTo = (route) => {
      assert.step(`transitionTo:${route}`);
    };

    await render(<template><RegistrationForm /></template>);
    await fillAndSubmit();

    // The redirect must come last — after the profile is patched and reloaded.
    // Redirecting earlier cancels the in-flight task and leaves the profile
    // blank (the bug this fixes).
    assert.verifySteps([
      'loadCurrentUser',
      'patchUserInfo',
      'loadCurrentUser',
      'transitionTo:index',
    ]);
  });

  test('still redirects into the app when the profile patch fails after auth', async function (assert) {
    // eslint-disable-next-line ember/no-classic-classes
    const MockFirebaseAuthenticator = EmberObject.extend({
      registerUser() {
        return Promise.resolve();
      },
    });

    class MockNetwork extends Service {
      loadCurrentUser() {
        return Promise.resolve();
      }
      loadCloudUrl() {
        return Promise.resolve();
      }
      patchUserInfo() {
        assert.step('patchUserInfo');
        // The account is already created/authenticated; only the profile save
        // fails (e.g. a transient backend error).
        return Promise.reject(
          Object.assign(new Error('save failed'), { errors: ['save failed'] }),
        );
      }
    }

    class MockSession extends Service {
      isAuthenticated = false;
      authenticate() {
        this.isAuthenticated = true;
        return Promise.resolve();
      }
    }

    this.owner.register('authenticator:firebase', MockFirebaseAuthenticator);
    this.owner.register('service:session', MockSession);
    this.owner.register('service:network', MockNetwork);

    this.owner.lookup('service:router').transitionTo = (route) => {
      assert.step(`transitionTo:${route}`);
    };

    await render(<template><RegistrationForm /></template>);
    await fillAndSubmit();

    // The registered+authenticated user is sent into the app instead of being
    // trapped on the form, even though the profile save failed.
    assert.verifySteps(['patchUserInfo', 'transitionTo:index']);
  });

  test('it able to handle registration error', async function (assert) {
    // eslint-disable-next-line ember/no-classic-classes
    const MockFirebaseAuthenticator = EmberObject.extend({
      registerUser() {
        assert.step('registerUser');
        return Promise.reject(new Error('foo'));
      },
    });

    class MockNetwork extends Service {
      loadCurrentUser() {
        return Promise.resolve();
      }
      loadCloudUrl() {
        return Promise.resolve();
      }
      patchUserInfo() {
        return Promise.resolve({});
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
    await fillAndSubmit();

    // registerUser fails, so login/patch never run.
    assert.verifySteps(['registerUser']);
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

  test('shows warning when password is shorter than 6 characters', async function (assert) {
    await render(<template><RegistrationForm /></template>);

    await fillIn('[name="password"]', 'Sho');

    assert.dom('[data-test-warning-message="password"]').exists('warning is shown for short password');
  });

  test('does not show password length warning when password is 6+ characters', async function (assert) {
    await render(<template><RegistrationForm /></template>);

    await fillIn('[name="password"]', 'Valid1');

    assert.dom('[data-test-warning-message="password"]').doesNotExist('no warning for valid password');
  });

  test('does not show password length warning when password is empty', async function (assert) {
    await render(<template><RegistrationForm /></template>);

    assert.dom('[data-test-warning-message="password"]').doesNotExist('no warning before input');
  });
});
