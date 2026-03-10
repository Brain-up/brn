import { module, test } from 'qunit';
import {
  loadHistory,
  saveHistoryEntry,
  clearHistory,
  generateEntryId,
} from 'brn/utils/audiometry-history-storage';

module('Unit | Utils | audiometry-history-storage', function (hooks) {
  hooks.beforeEach(function () {
    clearHistory();
  });

  hooks.afterEach(function () {
    clearHistory();
  });

  test('loadHistory returns empty array when no data', function (assert) {
    const history = loadHistory();
    assert.deepEqual(history, []);
  });

  test('saveHistoryEntry stores and loadHistory retrieves', function (assert) {
    const entry = {
      id: 'test-1',
      date: '2026-01-01T00:00:00Z',
      testId: '1',
      testName: 'Signal Test',
      audiometryType: 'SIGNALS',
      headphoneId: '5',
      executionSeconds: 120,
      leftEarThresholds: { 500: 25, 1000: 30 },
      rightEarThresholds: { 500: 20, 1000: 25 },
      ptaLeft: 27,
      ptaRight: 22,
      classificationLeft: 'mild_loss',
      classificationRight: 'normal',
    };
    saveHistoryEntry(entry);

    const history = loadHistory();
    assert.strictEqual(history.length, 1);
    assert.strictEqual(history[0].id, 'test-1');
    assert.strictEqual(history[0].testName, 'Signal Test');
    assert.deepEqual(history[0].leftEarThresholds, { 500: 25, 1000: 30 });
  });

  test('entries are ordered newest first', function (assert) {
    saveHistoryEntry({
      id: 'first',
      date: '2026-01-01T00:00:00Z',
      testId: '1',
      testName: 'First',
      audiometryType: 'SIGNALS',
      headphoneId: '5',
      executionSeconds: 60,
      leftEarThresholds: {},
      rightEarThresholds: {},
      ptaLeft: null,
      ptaRight: null,
      classificationLeft: null,
      classificationRight: null,
    });
    saveHistoryEntry({
      id: 'second',
      date: '2026-01-02T00:00:00Z',
      testId: '1',
      testName: 'Second',
      audiometryType: 'SIGNALS',
      headphoneId: '5',
      executionSeconds: 60,
      leftEarThresholds: {},
      rightEarThresholds: {},
      ptaLeft: null,
      ptaRight: null,
      classificationLeft: null,
      classificationRight: null,
    });

    const history = loadHistory();
    assert.strictEqual(history.length, 2);
    assert.strictEqual(history[0].id, 'second', 'newest entry is first');
    assert.strictEqual(history[1].id, 'first', 'oldest entry is last');
  });

  test('clearHistory removes all entries', function (assert) {
    saveHistoryEntry({
      id: 'test-1',
      date: '2026-01-01T00:00:00Z',
      testId: '1',
      testName: 'Test',
      audiometryType: 'SIGNALS',
      headphoneId: '5',
      executionSeconds: 60,
      leftEarThresholds: {},
      rightEarThresholds: {},
      ptaLeft: null,
      ptaRight: null,
      classificationLeft: null,
      classificationRight: null,
    });
    clearHistory();
    assert.deepEqual(loadHistory(), []);
  });

  test('generateEntryId returns unique strings', function (assert) {
    const id1 = generateEntryId();
    const id2 = generateEntryId();
    assert.notStrictEqual(id1, id2, 'IDs are unique');
    assert.ok(typeof id1 === 'string' && id1.length > 0, 'ID is a non-empty string');
  });

  test('loadHistory handles corrupted localStorage gracefully', function (assert) {
    localStorage.setItem('brn:audiometry-history', 'not-valid-json');
    const history = loadHistory();
    assert.deepEqual(history, [], 'returns empty array for invalid JSON');
  });

  test('loadHistory handles non-array localStorage gracefully', function (assert) {
    localStorage.setItem('brn:audiometry-history', '{"not": "array"}');
    const history = loadHistory();
    assert.deepEqual(history, [], 'returns empty array for non-array data');
  });

  test('max 50 entries are kept', function (assert) {
    for (let i = 0; i < 55; i++) {
      saveHistoryEntry({
        id: `entry-${i}`,
        date: new Date(2026, 0, i + 1).toISOString(),
        testId: '1',
        testName: 'Test',
        audiometryType: 'SIGNALS',
        headphoneId: '5',
        executionSeconds: 60,
        leftEarThresholds: {},
        rightEarThresholds: {},
        ptaLeft: null,
        ptaRight: null,
        classificationLeft: null,
        classificationRight: null,
      });
    }

    const history = loadHistory();
    assert.strictEqual(history.length, 50, 'max 50 entries kept');
    assert.strictEqual(history[0].id, 'entry-54', 'newest entry is first');
  });

  test('speech results are preserved', function (assert) {
    saveHistoryEntry({
      id: 'speech-1',
      date: '2026-01-01T00:00:00Z',
      testId: '2',
      testName: 'Speech Test',
      audiometryType: 'SPEECH',
      headphoneId: '5',
      executionSeconds: 90,
      leftEarThresholds: {},
      rightEarThresholds: {},
      ptaLeft: null,
      ptaRight: null,
      classificationLeft: null,
      classificationRight: null,
      speechResults: { correct: 8, total: 10 },
    });

    const history = loadHistory();
    assert.strictEqual(history[0].speechResults.correct, 8);
    assert.strictEqual(history[0].speechResults.total, 10);
  });
});
