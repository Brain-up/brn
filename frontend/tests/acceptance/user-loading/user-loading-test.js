import { module, test } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import { visit, settled } from '@ember/test-helpers';
import { setupMSW } from '../../helpers/msw';
import { authenticateSession } from 'ember-simple-auth/test-support';

module('Acceptance | user loading', function (hooks) {
  setupApplicationTest(hooks);
  setupMSW(hooks);

  test('loadCurrentUser is called exactly once when visiting an authenticated route', async function (assert) {
    let userCurrentCallCount = 0;

    server.get('users/current', () => {
      userCurrentCallCount++;
      return {
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
      };
    });

    server.get('groups', () => ({ data: [] }));

    await authenticateSession();
    await visit('/groups');
    await settled();

    assert.strictEqual(
      userCurrentCallCount,
      1,
      'GET /api/users/current called exactly once (from application route only)',
    );
  });

  test('loadCurrentUser is called exactly once when visiting a nested route', async function (assert) {
    let userCurrentCallCount = 0;

    const mockGroup = {
      id: '1',
      locale: 'en-us',
      name: 'Test Group',
      description: 'Test',
      series: [],
    };

    server.get('users/current', () => {
      userCurrentCallCount++;
      return {
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
      };
    });

    server.get('groups', () => ({ data: [mockGroup] }));
    server.get('groups/:id', () => ({ data: mockGroup }));
    server.get('series', () => ({ data: [] }));

    await authenticateSession();
    await visit('/groups/1');
    await settled();

    assert.strictEqual(
      userCurrentCallCount,
      1,
      'GET /api/users/current called exactly once even for nested group route',
    );
  });
});
