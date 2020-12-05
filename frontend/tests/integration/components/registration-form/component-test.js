import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, fillIn, click } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import Service from '@ember/service';

function getDate(num) {
  let date = new Date();
  return date.getFullYear() + num;
}

module('Integration | Component | registration-form', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<RegistrationForm />`);

    assert.dom('form').exists();
    assert.dom('[data-test-submit-form]').hasTagName('button');
    assert.dom('[name="firstName"]').hasAttribute('required');
    assert.dom('[data-test-form-warning]').doesNotExist();
  });

  test('it send register request if all fields filled', async function(assert) {
    assert.expect(4);
    class Network extends Service {
      createUser(fields) {
        assert.ok(fields);
        return {
          ok: true,
        };
      }
    }
    class MockSession extends Service {
      authenticate(type, login, password) {
        assert.ok(type);
        assert.ok(login);
        assert.ok(password);
      }
    }
    this.owner.register('service:session', MockSession);
    this.owner.register('service:network', Network);
    await render(hbs`<RegistrationForm />`);
    await fillIn('[name="firstName"]', 'b');
    await fillIn('[name="email"]', 'c@name.com');
    await fillIn('[name="password"]', 'd');
    await fillIn('[name="birthday"]', '1991');
    await click('[data-test-submit-form]');
  });

  test('it able to handle registration error', async function(assert) {
    assert.expect(3);
    class Network extends Service {
      createUser(fields) {
        assert.ok(fields);
        return {
          ok: false,
          json() {
            assert.ok(fields);
            return {
              errors: ['foo'],
            };
          },
        };
      }
    }
    this.owner.register('service:network', Network);
    await render(hbs`<RegistrationForm />`);
    await fillIn('[name="firstName"]', 'b');
    await fillIn('[name="email"]', 'c@name.com');
    await fillIn('[name="password"]', 'd');
    await fillIn('[name="birthday"]', '1991');
    await click('[data-test-submit-form]');
    assert.dom('[data-test-form-error]').hasText('foo');
  });

  test('show message when entering date below acceptable', async function(assert) {
    await render(hbs`<RegistrationForm />`);

    await fillIn('input[name="birthday"]', '1911');

    assert.dom('[data-test-warning-message="birthday"]').exists();
  });

  test('show message when entering date higher than allowed', async function(assert) {
    await render(hbs`<RegistrationForm />`);

    await fillIn('input[name="birthday"]', getDate(1));

    assert.dom('[data-test-warning-message="birthday"]').exists();
  });

  test('do not show message when entering valid date', async function(assert) {
    await render(hbs`<RegistrationForm />`);

    await fillIn('input[name="birthday"]', getDate(0));

    assert.dom('[data-test-warning-message="birthday"]').doesNotExist();
  });
});
