import { module, test } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import { visit, click, fillIn } from '@ember/test-helpers';
import { setupMSW } from '../../helpers/msw';
import { authenticateSession } from 'ember-simple-auth/test-support';

module('Acceptance | headphones', function (hooks) {
  setupApplicationTest(hooks);
  setupMSW(hooks);

  hooks.beforeEach(async function () {
    server.get('users/current', () => ({
      data: [
        {
          id: '1',
          name: 'Test User',
          email: 'test@test.com',
          bornYear: 1990,
          gender: 'MALE',
          active: true,
          avatar: '1',
          roles: ['ROLE_USER'],
        },
      ],
      errors: [],
      meta: [],
    }));
  });

  test('shows empty headphones list on profile', async function (assert) {
    server.get('users/current/headphones', () => ({ data: [] }));

    await authenticateSession();
    await visit('/profile');

    assert.dom('[data-test-headphone-item]').doesNotExist('no headphones shown when empty');
    assert.dom('[data-test-show-add-headphones]').exists('add button is shown');
  });

  test('shows headphones list when headphones exist', async function (assert) {
    server.get('users/current/headphones', () => ({
      data: [
        { id: '1', name: 'Sony WH-1000XM5', active: true, type: 'OVER_EAR_BLUETOOTH', description: '', userAccount: '1' },
        { id: '2', name: 'AirPods Pro', active: true, type: 'IN_EAR_BLUETOOTH', description: '', userAccount: '1' },
      ],
    }));

    await authenticateSession();
    await visit('/profile');

    assert.dom('[data-test-headphone-item]').exists({ count: 2 }, 'two headphones shown');
    assert.dom('[data-test-headphone-name]').exists({ count: 2 });
  });

  test('can add headphones via form', async function (assert) {
    let postCalled = false;
    let postData = null;

    server.get('users/current/headphones', () => ({ data: [] }));
    server.post('users/current/headphones', (request) => {
      postCalled = true;
      postData = JSON.parse(request.requestBody);
      return { data: { id: '1', name: postData.name, active: true, type: postData.type } };
    });

    await authenticateSession();
    await visit('/profile');

    await click('[data-test-show-add-headphones]');
    assert.dom('[data-test-add-headphones-form]').exists('form is shown');

    await fillIn('[data-test-headphone-name-input]', 'My New Headphones');
    await click('[data-test-submit-headphone]');
    

    assert.true(postCalled, 'POST was called');
    assert.strictEqual(postData.name, 'My New Headphones', 'name sent correctly');
  });

  test('shows validation error for empty name', async function (assert) {
    server.get('users/current/headphones', () => ({ data: [] }));

    await authenticateSession();
    await visit('/profile');

    await click('[data-test-show-add-headphones]');
    await click('[data-test-submit-headphone]');

    assert.dom('[data-test-headphone-error]').exists('error message shown');
  });

  test('can delete headphones', async function (assert) {
    let deleteCalled = false;

    server.get('users/current/headphones', () => ({
      data: [
        { id: '1', name: 'Test HP', active: true, type: 'NOT_DEFINED', description: '', userAccount: '1' },
      ],
    }));
    server.delete('users/current/headphones/:id', () => {
      deleteCalled = true;
      return {};
    });

    await authenticateSession();
    await visit('/profile');

    assert.dom('[data-test-headphone-item]').exists({ count: 1 });

    await click('[data-test-delete-headphone]');

    assert.dom('[data-test-confirm-dialog]').exists('confirm dialog shown');
    assert.dom('[data-test-confirm-message]').hasText(/./);

    await click('[data-test-confirm-ok]');

    assert.true(deleteCalled, 'DELETE was called');
  });

  test('cancel button hides the add form', async function (assert) {
    server.get('users/current/headphones', () => ({ data: [] }));

    await authenticateSession();
    await visit('/profile');

    await click('[data-test-show-add-headphones]');
    assert.dom('[data-test-add-headphones-form]').exists();

    await click('[data-test-cancel-headphone]');
    assert.dom('[data-test-add-headphones-form]').doesNotExist('form hidden after cancel');
  });
});
