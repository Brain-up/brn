import { module, test } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import { visit, click, fillIn, currentURL } from '@ember/test-helpers';
import { setupMSW } from '../../helpers/msw';
import { authenticateSession } from 'ember-simple-auth/test-support';

module('Acceptance | doctor | patients', function (hooks) {
  setupApplicationTest(hooks);
  setupMSW(hooks);

  const specialistUser = () => ({
    data: [
      {
        id: '1',
        name: 'Doctor User',
        email: 'doc@test.com',
        bornYear: 1980,
        gender: 'MALE',
        active: true,
        avatar: '1',
        roles: ['ROLE_USER', 'ROLE_SPECIALIST'],
      },
    ],
    errors: [],
    meta: [],
  });

  const regularUser = () => ({
    data: [
      {
        id: '2',
        name: 'Regular User',
        email: 'user@test.com',
        bornYear: 1990,
        gender: 'FEMALE',
        active: true,
        avatar: '1',
        roles: ['ROLE_USER'],
      },
    ],
    errors: [],
    meta: [],
  });

  test('specialist can see patient list', async function (assert) {
    server.get('users/current', specialistUser);
    server.get('doctors/:doctorId/patients', () => ({
      data: [
        { id: '10', name: 'Ivan Petrov', email: 'ivan@test.com' },
        { id: '11', name: 'Anna Sidorova', email: 'anna@test.com' },
      ],
    }));

    await authenticateSession();
    await visit('/doctor/patients');

    assert.dom('[data-test-patient-card]').exists({ count: 2 }, 'two patient cards shown');
  });

  test('non-specialist is redirected away', async function (assert) {
    server.get('users/current', regularUser);

    await authenticateSession();
    await visit('/doctor/patients');

    assert.notEqual(currentURL(), '/doctor/patients', 'non-specialist was redirected');
  });

  test('specialist can add a patient', async function (assert) {
    let postCalled = false;
    let postPayload = null;

    server.get('users/current', specialistUser);
    server.get('doctors/:doctorId/patients', () => ({ data: [] }));
    server.post('doctors/:doctorId/patients', (request) => {
      postCalled = true;
      postPayload = JSON.parse(request.requestBody);
      return { data: { id: postPayload.id, name: 'New Patient' } };
    });

    await authenticateSession();
    await visit('/doctor/patients');

    await click('[data-test-show-add-patient]');
    assert.dom('[data-test-add-patient-form]').exists('add form shown');

    await fillIn('[data-test-patient-id-input]', '42');
    await click('[data-test-submit-patient]');
    

    assert.true(postCalled, 'POST was called');
    assert.strictEqual(postPayload.id, '42', 'patient ID sent correctly');
    assert.strictEqual(postPayload.type, 'PATIENT', 'patient type sent correctly');
  });

  test('specialist can remove a patient', async function (assert) {
    let deleteCalled = false;

    server.get('users/current', specialistUser);
    server.get('doctors/:doctorId/patients', () => ({
      data: [
        { id: '10', name: 'Ivan Petrov', email: 'ivan@test.com' },
      ],
    }));
    server.delete('doctors/:doctorId/patients/:patientId', () => {
      deleteCalled = true;
      return {};
    });

    await authenticateSession();
    await visit('/doctor/patients');

    assert.dom('[data-test-patient-card]').exists({ count: 1 });

    await click('[data-test-remove-patient]');

    assert.dom('[data-test-confirm-dialog]').exists('confirm dialog shown');

    await click('[data-test-confirm-ok]');

    assert.true(deleteCalled, 'DELETE was called');
  });

  test('shows empty state when specialist has no patients', async function (assert) {
    server.get('users/current', specialistUser);
    server.get('doctors/:doctorId/patients', () => ({ data: [] }));

    await authenticateSession();
    await visit('/doctor/patients');

    assert.dom('[data-test-patient-card]').doesNotExist('no patient cards');
  });
});
