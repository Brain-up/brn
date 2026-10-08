import { module, test } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import { visit } from '@ember/test-helpers';
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


    assert.strictEqual(
      userCurrentCallCount,
      1,
      'GET /api/users/current called exactly once (from application route only)',
    );
  });

  test('loadTodayCompletedExercises is called after user loads', async function (assert) {
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

    server.get('groups', () => ({ data: [] }));

    server.get('v2/study-history/histories', () => ({
      data: [
        { exerciseId: 42, startTime: '2024-01-01T10:00:00', endTime: '2024-01-01T10:05:00' },
        { exerciseId: 99, startTime: '2024-01-01T11:00:00', endTime: '2024-01-01T11:05:00' },
      ],
    }));

    await authenticateSession();
    await visit('/groups');

    const tasksManager = this.owner.lookup('service:tasks-manager');
    assert.true(
      tasksManager.completedExerciseIds.has('42'),
      'completedExerciseIds contains exercise 42',
    );
    assert.true(
      tasksManager.completedExerciseIds.has('99'),
      'completedExerciseIds contains exercise 99',
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


    assert.strictEqual(
      userCurrentCallCount,
      1,
      'GET /api/users/current called exactly once even for nested group route',
    );
  });
});
