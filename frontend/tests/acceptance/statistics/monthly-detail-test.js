import { module, test } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import { visit, waitFor } from '@ember/test-helpers';
import { setupMSW } from '../../helpers/msw';
import { authenticateSession } from 'ember-simple-auth/test-support';

module('Acceptance | statistics | monthly detail', function (hooks) {
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

  test('shows monthly detail table with data', async function (assert) {
    server.get('study-history/monthHistories', () => ({
      data: [
        {
          id: '1',
          exerciseId: '42',
          startTime: '2024-03-01T10:00:00',
          endTime: '2024-03-01T10:05:32',
          executionSeconds: 332,
          tasksCount: 20,
          replaysCount: 2,
          wrongAnswers: 3,
        },
        {
          id: '2',
          exerciseId: '43',
          startTime: '2024-03-02T09:00:00',
          endTime: '2024-03-02T09:04:48',
          executionSeconds: 288,
          tasksCount: 10,
          replaysCount: 0,
          wrongAnswers: 0,
        },
      ],
    }));

    await authenticateSession();
    await visit('/profile/statistics');

    await waitFor('[data-test-monthly-detail-table]', { timeout: 3000 });
    assert.dom('[data-test-monthly-detail-table]').exists('monthly detail table is rendered');
    assert.dom('[data-test-monthly-detail-row]').exists({ count: 2 }, 'two rows shown');
  });

  test('shows empty state when no monthly data', async function (assert) {
    server.get('study-history/monthHistories', () => ({ data: [] }));

    await authenticateSession();
    await visit('/profile/statistics');

    assert.dom('[data-test-monthly-detail-table]').doesNotExist('no table when empty');
  });
});
