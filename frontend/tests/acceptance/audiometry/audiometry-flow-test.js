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

  test('SIGNALS test player flow — plays per frequency/ear', async function (assert) {
    const historyPosts = [];

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
          { id: '101', ear: 'LEFT', frequencies: [125, 250] },
          { id: '102', ear: 'RIGHT', frequencies: [125] },
        ],
      },
    }));
    server.get('users/current/headphones', () => ({
      data: [{ id: '5', name: 'Test HP', active: true, type: 'NOT_DEFINED' }],
    }));
    server.post('audiometry-history', (request) => {
      historyPosts.push(JSON.parse(request.requestBody));
      return { data: { id: '1' } };
    });

    await authenticateSession();
    await visit('/audiometry/1');

    // Setup phase — select headphones
    assert.dom('[data-test-headphone-select]').exists('headphone selector shown');

    const select = document.querySelector('[data-test-headphone-select]');
    select.value = '5';
    select.dispatchEvent(new Event('change', { bubbles: true }));
    await settled();

    // Start test
    await click('[data-test-start-audiometry]');

    // Task 1 (LEFT ear), frequency 125 — ear label shown
    assert.dom('[data-test-ear-label]').exists('ear label shown');
    assert.dom('[data-test-frequency-info]').exists('frequency info shown');
    assert.dom('[data-test-answer-yes]').exists('yes button shown');

    // Answer YES to freq 125 LEFT
    await click('[data-test-answer-yes]');

    // Answer NO to freq 250 LEFT
    await click('[data-test-answer-no]');

    // Task 2 (RIGHT ear), frequency 125
    // Answer YES to freq 125 RIGHT
    await click('[data-test-answer-yes]');

    // Should be on results
    assert.dom('[data-test-back-to-list]').exists('back button shown on results');
    assert.dom('[data-test-signal-ear-result]').exists({ count: 2 }, 'per-ear results shown');

    // Verify history posts
    assert.strictEqual(historyPosts.length, 2, 'two history posts made (one per ear task)');

    const leftPost = historyPosts.find((p) => p.audiometryTaskId === '101');
    assert.ok(leftPost, 'LEFT ear history posted');
    assert.strictEqual(leftPost.headphones, '5', 'correct headphones ID');
    assert.strictEqual(leftPost.tasksCount, 2, 'LEFT task has 2 frequencies');
    assert.strictEqual(leftPost.rightAnswers, 1, 'LEFT: 1 heard (only 125)');
    assert.deepEqual(leftPost.sinAudiometryResults, { 125: 50 }, 'sinAudiometryResults has heard freq at 50dB');

    const rightPost = historyPosts.find((p) => p.audiometryTaskId === '102');
    assert.ok(rightPost, 'RIGHT ear history posted');
    assert.strictEqual(rightPost.rightAnswers, 1, 'RIGHT: 1 heard');
    assert.deepEqual(rightPost.sinAudiometryResults, { 125: 50 }, 'RIGHT sinAudiometryResults correct');
  });

  test('SPEECH test player flow — word selection', async function (assert) {
    const historyPosts = [];

    server.get('audiometrics', () => ({
      data: [
        { id: '2', locale: 'en-us', name: 'Speech Test', audiometryType: 'SPEECH', description: 'Speech test' },
      ],
    }));
    server.get('audiometrics/:id', () => ({
      data: {
        id: '2',
        locale: 'en-us',
        name: 'Speech Test',
        audiometryType: 'SPEECH',
        description: 'Speech test',
        audiometryTasks: [
          {
            id: 201,
            level: 1,
            audiometryGroup: 'A',
            frequencyZone: 'LOW',
            minFrequency: 150,
            maxFrequency: 400,
            count: 1,
            showSize: 3,
            answerOptions: [
              { id: 1, word: 'apple', wordType: 'AUDIOMETRY_WORD', locale: 'en-us', pictureFileUrl: '', soundsCount: 0, description: '' },
              { id: 2, word: 'chair', wordType: 'AUDIOMETRY_WORD', locale: 'en-us', pictureFileUrl: '', soundsCount: 0, description: '' },
              { id: 3, word: 'table', wordType: 'AUDIOMETRY_WORD', locale: 'en-us', pictureFileUrl: '', soundsCount: 0, description: '' },
            ],
          },
        ],
      },
    }));
    server.get('users/current/headphones', () => ({
      data: [{ id: '5', name: 'Test HP', active: true, type: 'NOT_DEFINED' }],
    }));
    server.post('audiometry-history', (request) => {
      historyPosts.push(JSON.parse(request.requestBody));
      return { data: { id: '1' } };
    });

    await authenticateSession();
    await visit('/audiometry/2');

    // Setup phase — select headphones
    const select = document.querySelector('[data-test-headphone-select]');
    select.value = '5';
    select.dispatchEvent(new Event('change', { bubbles: true }));
    await settled();

    // Start test
    await click('[data-test-start-audiometry]');

    // Speech UI — word buttons shown
    assert.dom('[data-test-speech-word]').exists({ count: 3 }, '3 word buttons shown');

    // Click first word button (any word)
    await click('[data-test-speech-word]');

    // Should be on results
    assert.dom('[data-test-back-to-list]').exists('back button shown on results');

    // Verify history post
    assert.strictEqual(historyPosts.length, 1, 'one history post made');
    assert.strictEqual(historyPosts[0].audiometryTaskId, '201', 'correct task ID');
    assert.strictEqual(historyPosts[0].headphones, '5', 'correct headphones');
    assert.strictEqual(historyPosts[0].tasksCount, 1, 'correct tasks count');
  });

  test('MATRIX test — shows unavailable message', async function (assert) {
    server.get('audiometrics', () => ({
      data: [
        { id: '3', locale: 'en-us', name: 'Matrix Test', audiometryType: 'MATRIX', description: 'Matrix test' },
      ],
    }));
    server.get('audiometrics/:id', () => ({
      data: {
        id: '3',
        locale: 'en-us',
        name: 'Matrix Test',
        audiometryType: 'MATRIX',
        description: 'Matrix test',
        audiometryTasks: [],
      },
    }));
    server.get('users/current/headphones', () => ({
      data: [{ id: '5', name: 'Test HP', active: true, type: 'NOT_DEFINED' }],
    }));

    await authenticateSession();
    await visit('/audiometry/3');

    assert.dom('[data-test-matrix-unavailable]').exists('matrix unavailable message shown');
    assert.dom('[data-test-start-audiometry]').doesNotExist('no start button for matrix');
    assert.dom('[data-test-headphone-select]').doesNotExist('no headphone select for matrix');
  });
});
