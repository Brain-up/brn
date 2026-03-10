import { module, test } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import { visit, click, settled } from '@ember/test-helpers';
import { setupMSW } from '../../helpers/msw';
import { authenticateSession } from 'ember-simple-auth/test-support';

module('Acceptance | audiometry | flow', function (hooks) {
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

  test('shows audiometry test list', async function (assert) {
    server.get('audiometrics', () => ({
      data: [
        { id: '1', locale: 'en-us', name: 'Signal Test', audiometryType: 'SIGNALS', description: 'Frequency test' },
        { id: '2', locale: 'en-us', name: 'Speech Test', audiometryType: 'SPEECH', description: 'Speech test' },
      ],
    }));
    server.get('users/current/headphones', () => ({
      data: [{ id: '1', name: 'Test HP', active: true, type: 'NOT_DEFINED' }],
    }));

    await authenticateSession();
    await visit('/audiometry');

    assert.dom('[data-test-audiometry-card]').exists({ count: 2 }, 'two test cards shown');
    assert.dom('[data-test-start-test]').exists({ count: 2 }, 'start buttons shown');
    assert.dom('[data-test-no-headphones-warning]').doesNotExist('no headphones warning not shown');
  });

  test('shows headphones warning when no headphones configured', async function (assert) {
    server.get('audiometrics', () => ({
      data: [
        { id: '1', locale: 'en-us', name: 'Signal Test', audiometryType: 'SIGNALS', description: 'Frequency test' },
      ],
    }));
    server.get('users/current/headphones', () => ({ data: [] }));

    await authenticateSession();
    await visit('/audiometry');

    assert.dom('[data-test-no-headphones-warning]').exists('headphones warning is shown');
    assert.dom('[data-test-start-test]').doesNotExist('start button not shown without headphones');
  });

  test('audiometry test player flow', async function (assert) {
    let historyPostCalled = false;
    let historyPayload = null;

    server.get('audiometrics', () => ({
      data: [
        { id: '1', locale: 'en-us', name: 'Signal Test', audiometryType: 'SIGNALS', description: 'Frequency test' },
      ],
    }));
    server.get('audiometrics/:id', () => ({
      data: {
        id: '1',
        locale: 'en-us',
        name: 'Signal Test',
        audiometryType: 'SIGNALS',
        description: 'Frequency test',
        audiometryTasks: [
          { id: '101', frequencyZone: 1 },
          { id: '102', frequencyZone: 2 },
        ],
      },
    }));
    server.get('users/current/headphones', () => ({
      data: [{ id: '5', name: 'Test HP', active: true, type: 'NOT_DEFINED' }],
    }));
    server.post('audiometry-history', (request) => {
      historyPostCalled = true;
      historyPayload = JSON.parse(request.requestBody);
      return { data: { id: '1' } };
    });

    await authenticateSession();
    await visit('/audiometry/1');

    // Should be on setup phase - select headphones
    assert.dom('[data-test-headphone-select]').exists('headphone selector shown');

    // Select headphones
    const select = document.querySelector('[data-test-headphone-select]');
    select.value = '5';
    select.dispatchEvent(new Event('change', { bubbles: true }));
    await settled();

    // Start test
    await click('[data-test-start-audiometry]');

    // Answer tasks
    assert.dom('[data-test-answer-yes]').exists('yes button shown');
    await click('[data-test-answer-yes]');

    await click('[data-test-answer-no]');
    

    // Should be on results
    assert.true(historyPostCalled, 'audiometry history was posted');
    assert.strictEqual(historyPayload.audiometryTaskId, '101', 'sends first task ID, not parent test ID');
    assert.strictEqual(historyPayload.headphones, '5', 'correct headphones ID sent');
    assert.strictEqual(historyPayload.tasksCount, 2, 'correct task count');
    assert.strictEqual(historyPayload.rightAnswers, 1, 'correct right answers count');

    assert.dom('[data-test-back-to-list]').exists('back button shown on results');
  });
});
