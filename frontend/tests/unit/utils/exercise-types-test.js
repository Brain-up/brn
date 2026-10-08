import { module, test } from 'qunit';
import { ExerciseMechanism } from 'brn/utils/exercise-types';

module('Unit | Utility | exercise-types', function () {
  test('ExerciseMechanism.WORDS has correct value', function (assert) {
    assert.strictEqual(ExerciseMechanism.WORDS, 'WORDS');
  });

  test('ExerciseMechanism.MATRIX has correct value', function (assert) {
    assert.strictEqual(ExerciseMechanism.MATRIX, 'MATRIX');
  });

  test('ExerciseMechanism.SIGNALS has correct value', function (assert) {
    assert.strictEqual(ExerciseMechanism.SIGNALS, 'SIGNALS');
  });

  test('ExerciseMechanism contains exactly seven members', function (assert) {
    const values = Object.values(ExerciseMechanism);
    assert.strictEqual(values.length, 7);
    assert.deepEqual(values.sort(), [
      'AUDITORY_SEQUENCE',
      'ENVIRONMENTAL_SOUNDS',
      'MATRIX',
      'PHONEME_PAIRS',
      'PROSODY',
      'SIGNALS',
      'WORDS',
    ]);
  });
});
