import { module, test } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import { visit, click, triggerKeyEvent, find } from '@ember/test-helpers';
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

  test('SIGNALS test player — adaptive threshold flow', async function (assert) {
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

    // Setup phase — single headphone auto-selected
    assert.dom('[data-test-headphone-select]').exists('headphone selector shown');
    assert.dom('[data-test-start-audiometry]').isNotDisabled('start button enabled with auto-selected headphone');

    // Start test
    await click('[data-test-start-audiometry]');

    // Task 1 (LEFT ear), frequency 125 — ear label and dB info shown
    assert.dom('[data-test-ear-label]').exists('ear label shown');
    assert.dom('[data-test-frequency-info]').exists('frequency info shown');
    assert.dom('[data-test-db-level]').exists('dB level shown');
    assert.dom('[data-test-answer-yes]').exists('yes button shown');
    assert.dom('[data-test-replay-tone]').exists('replay tone button shown');

    // In test mode, tone playback is skipped so buttons are immediately active.
    // Adaptive flow: keep answering YES — threshold should be determined quickly
    // since hearing at decreasing levels eventually finds threshold.

    // Answer YES at 40 dB → drops to 30
    await click('[data-test-answer-yes]');
    // Answer YES at 30 → drops to 20
    await click('[data-test-answer-yes]');
    // Answer NO at 20 → rises to 25 (reversal 1)
    await click('[data-test-answer-no]');
    // Answer YES at 25 → drops to 15 (reversal 2, ascending heard at 25: count=1)
    await click('[data-test-answer-yes]');
    // Answer NO at 15 → rises to 20 (reversal 3)
    await click('[data-test-answer-no]');
    // Answer YES at 20 → drops to 10 (reversal 4, ascending heard at 20: count=1)
    await click('[data-test-answer-yes]');
    // Answer NO at 10 → rises to 15 (reversal 5)
    await click('[data-test-answer-no]');
    // Answer YES at 15 → (reversal 6, ascending heard at 15: count=1)
    await click('[data-test-answer-yes]');
    // Answer NO → rises (reversal 7)
    await click('[data-test-answer-no]');
    // Answer YES → ascending heard count increases (reversal 8 → MAX_REVERSALS reached)
    await click('[data-test-answer-yes]');

    // After MAX_REVERSALS, freq 125 LEFT should be complete.
    // Continue answering for freq 250 LEFT (same pattern, abbreviated)
    // Keep answering YES to quickly reach threshold
    for (let i = 0; i < 10; i++) {
      if (document.querySelector('[data-test-back-to-list]')) break;
      if (document.querySelector('[data-test-answer-yes]')) {
        await click('[data-test-answer-yes]');
      }
    }

    // Continue for RIGHT ear freq 125
    for (let i = 0; i < 10; i++) {
      if (document.querySelector('[data-test-back-to-list]')) break;
      if (document.querySelector('[data-test-answer-yes]')) {
        await click('[data-test-answer-yes]');
      }
    }

    // Handle remaining clicks if needed (adaptive needs more)
    for (let i = 0; i < 20; i++) {
      if (document.querySelector('[data-test-back-to-list]')) break;
      if (document.querySelector('[data-test-answer-yes]')) {
        await click('[data-test-answer-yes]');
      }
    }

    // Should be on results
    assert.dom('[data-test-back-to-list]').exists('back button shown on results');
    assert.dom('[data-test-signal-ear-result]').exists({ count: 2 }, 'per-ear results shown');
    assert.dom('[data-test-audiogram]').exists('audiogram chart rendered on results');

    // Verify history posts
    assert.strictEqual(historyPosts.length, 2, 'two history posts made (one per ear task)');

    const leftPost = historyPosts.find((p) => p.audiometryTaskId === '101');
    assert.ok(leftPost, 'LEFT ear history posted');
    assert.strictEqual(leftPost.headphones, '5', 'correct headphones ID');
    assert.strictEqual(leftPost.tasksCount, 2, 'LEFT task has 2 frequencies');
    assert.ok(leftPost.sinAudiometryResults, 'sinAudiometryResults present');
    // Thresholds should be actual measured dB values (not fixed 50)
    if (leftPost.sinAudiometryResults[125] !== undefined) {
      assert.ok(leftPost.sinAudiometryResults[125] >= 0 && leftPost.sinAudiometryResults[125] <= 90,
        'threshold for 125 Hz is in valid dB range');
    }

    const rightPost = historyPosts.find((p) => p.audiometryTaskId === '102');
    assert.ok(rightPost, 'RIGHT ear history posted');
    assert.ok(rightPost.sinAudiometryResults, 'RIGHT sinAudiometryResults present');
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

    // Setup phase — single headphone auto-selected, start directly
    await click('[data-test-start-audiometry]');

    // Speech UI — word buttons shown
    assert.dom('[data-test-speech-word]').exists({ count: 3 }, '3 word buttons shown');
    assert.dom('[data-test-replay-word]').exists('replay button shown');

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

  test('SIGNALS keyboard shortcuts — ArrowLeft=yes, ArrowRight=no, ArrowUp=replay', async function (assert) {
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
          { id: '101', ear: 'LEFT', frequencies: [1000] },
        ],
      },
    }));
    server.get('users/current/headphones', () => ({
      data: [{ id: '5', name: 'Test HP', active: true, type: 'NOT_DEFINED' }],
    }));
    server.post('audiometry-history', () => ({ data: { id: '1' } }));

    await authenticateSession();
    await visit('/audiometry/1');
    await click('[data-test-start-audiometry]');

    // Verify testing phase is active
    assert.dom('[data-test-ear-label]').exists('testing phase active');
    assert.dom('[data-test-answer-yes]').exists('yes button visible');

    // Read initial dB level
    const initialDB = find('[data-test-db-level]').textContent.trim();

    // ArrowUp should replay (dB level stays the same)
    const container = find('.outline-none[tabindex="0"]');
    await triggerKeyEvent(container, 'keydown', 'ArrowUp');
    assert.dom('[data-test-db-level]').hasText(initialDB, 'ArrowUp replays — dB unchanged');
    assert.dom('[data-test-answer-yes]').exists('still in testing after replay');

    // ArrowLeft should act as "Yes" (heard) — dB level decreases
    await triggerKeyEvent(container, 'keydown', 'ArrowLeft');
    const afterYesDB = find('[data-test-db-level]').textContent.trim();
    assert.notEqual(afterYesDB, initialDB, 'ArrowLeft (yes) changed the dB level');

    // ArrowRight should act as "No" (not heard) — dB level changes (increases)
    const beforeNoDB = find('[data-test-db-level]').textContent.trim();
    await triggerKeyEvent(container, 'keydown', 'ArrowRight');
    const afterNoDB = find('[data-test-db-level]').textContent.trim();
    assert.notEqual(afterNoDB, beforeNoDB, 'ArrowRight (no) changed the dB level');
  });

  test('SIGNALS keyboard shortcuts complete full adaptive test', async function (assert) {
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
          { id: '101', ear: 'LEFT', frequencies: [1000] },
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
    await click('[data-test-start-audiometry]');

    const container = find('.outline-none[tabindex="0"]');

    // Use keyboard to complete the adaptive procedure (same YES/NO pattern as click test)
    // ArrowLeft = Yes, ArrowRight = No
    await triggerKeyEvent(container, 'keydown', 'ArrowLeft');  // YES at 40
    await triggerKeyEvent(container, 'keydown', 'ArrowLeft');  // YES at 30
    await triggerKeyEvent(container, 'keydown', 'ArrowRight'); // NO at 20
    await triggerKeyEvent(container, 'keydown', 'ArrowLeft');  // YES at 25
    await triggerKeyEvent(container, 'keydown', 'ArrowRight'); // NO at 15
    await triggerKeyEvent(container, 'keydown', 'ArrowLeft');  // YES at 20
    await triggerKeyEvent(container, 'keydown', 'ArrowRight'); // NO at 10
    await triggerKeyEvent(container, 'keydown', 'ArrowLeft');  // YES at 15
    await triggerKeyEvent(container, 'keydown', 'ArrowRight'); // NO
    await triggerKeyEvent(container, 'keydown', 'ArrowLeft');  // YES → threshold reached

    // Handle any remaining iterations needed
    for (let i = 0; i < 20; i++) {
      if (document.querySelector('[data-test-back-to-list]')) break;
      if (document.querySelector('[data-test-answer-yes]')) {
        await triggerKeyEvent(container, 'keydown', 'ArrowLeft');
      }
    }

    // Should reach results phase
    assert.dom('[data-test-back-to-list]').exists('results phase reached via keyboard');
    assert.dom('[data-test-audiogram]').exists('audiogram shown on results');
    assert.dom('[data-test-signal-ear-result]').exists({ count: 1 }, 'ear result shown');
    assert.strictEqual(historyPosts.length, 1, 'history posted for the ear task');
    assert.ok(historyPosts[0].sinAudiometryResults, 'threshold results saved');
  });

  test('keyboard shortcuts do not interfere during setup phase', async function (assert) {
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
          { id: '101', ear: 'LEFT', frequencies: [1000] },
        ],
      },
    }));
    server.get('users/current/headphones', () => ({
      data: [{ id: '5', name: 'Test HP', active: true, type: 'NOT_DEFINED' }],
    }));

    await authenticateSession();
    await visit('/audiometry/1');

    // Still in setup phase — keyboard should not trigger any test actions
    const container = find('.outline-none[tabindex="0"]');
    await triggerKeyEvent(container, 'keydown', 'ArrowLeft');
    await triggerKeyEvent(container, 'keydown', 'ArrowRight');
    await triggerKeyEvent(container, 'keydown', 'ArrowUp');

    // Should still be in setup, not testing
    assert.dom('[data-test-start-audiometry]').exists('still in setup phase after key presses');
    assert.dom('[data-test-ear-label]').doesNotExist('not in testing phase');
  });

  test('keyboard hint is shown during SIGNALS testing', async function (assert) {
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
          { id: '101', ear: 'LEFT', frequencies: [1000] },
        ],
      },
    }));
    server.get('users/current/headphones', () => ({
      data: [{ id: '5', name: 'Test HP', active: true, type: 'NOT_DEFINED' }],
    }));
    server.post('audiometry-history', () => ({ data: { id: '1' } }));

    await authenticateSession();
    await visit('/audiometry/1');
    await click('[data-test-start-audiometry]');

    assert.dom('[data-test-ear-label]').exists('testing phase active');
    assert.dom('[data-test-keyboard-hint]').exists('keyboard hint is shown during testing');
  });
});
