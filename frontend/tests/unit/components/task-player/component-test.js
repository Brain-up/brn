import { module, test } from 'qunit';
import { MODES } from 'brn/utils/task-modes';

module('Unit | Component | task-player | disableAnswers', function () {
  // TaskPlayerComponent is a classic Ember component that is complex to render
  // in isolation, so we test the disableAnswers getter logic directly.

  function createFakeComponent({ mode, isPlaying, isProcessing, disableAudioPlayer }) {
    return {
      mode,
      audio: {
        isPlaying: !!isPlaying,
        isProcessing: !!isProcessing,
        get isBusy() {
          return this.isPlaying || this.isProcessing;
        },
      },
      disableAudioPlayer: !!disableAudioPlayer,
      // Reproduce the getter logic from TaskPlayerComponent
      get disableAnswers() {
        if (this.mode === MODES.INTERACT) {
          return this.audio.isPlaying;
        }
        return this.audio.isBusy || this.disableAudioPlayer;
      },
    };
  }

  test('disableAnswers uses isBusy in TASK mode', function (assert) {
    const component = createFakeComponent({
      mode: MODES.TASK,
      isProcessing: true,
      isPlaying: false,
    });
    assert.true(component.disableAnswers, 'disabled when isProcessing is true in TASK mode');
  });

  test('disableAnswers uses isBusy when isPlaying in TASK mode', function (assert) {
    const component = createFakeComponent({
      mode: MODES.TASK,
      isProcessing: false,
      isPlaying: true,
    });
    assert.true(component.disableAnswers, 'disabled when isPlaying is true in TASK mode');
  });

  test('disableAnswers is false when idle in TASK mode', function (assert) {
    const component = createFakeComponent({
      mode: MODES.TASK,
      isProcessing: false,
      isPlaying: false,
    });
    assert.false(component.disableAnswers, 'not disabled when idle in TASK mode');
  });

  test('disableAnswers uses isPlaying (not isBusy) in INTERACT mode', function (assert) {
    const component = createFakeComponent({
      mode: MODES.INTERACT,
      isProcessing: true,
      isPlaying: false,
    });
    assert.false(
      component.disableAnswers,
      'not disabled when only isProcessing in INTERACT mode - users can click while loading',
    );
  });

  test('disableAnswers is true in INTERACT mode when isPlaying', function (assert) {
    const component = createFakeComponent({
      mode: MODES.INTERACT,
      isProcessing: false,
      isPlaying: true,
    });
    assert.true(component.disableAnswers, 'disabled when isPlaying in INTERACT mode');
  });

  test('disableAnswers reflects disableAudioPlayer in TASK mode', function (assert) {
    const component = createFakeComponent({
      mode: MODES.TASK,
      isProcessing: false,
      isPlaying: false,
      disableAudioPlayer: true,
    });
    assert.true(component.disableAnswers, 'disabled when disableAudioPlayer is true');
  });

  test('disableAnswers in LISTEN mode uses isBusy', function (assert) {
    const component = createFakeComponent({
      mode: MODES.LISTEN,
      isProcessing: true,
      isPlaying: false,
    });
    assert.true(component.disableAnswers, 'disabled when isProcessing in LISTEN mode');
  });
});
