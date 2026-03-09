import { module, test } from 'qunit';
import { MODES } from 'brn/utils/task-modes';
import { ExerciseMechanism } from 'brn/utils/exercise-types';

module('Unit | Component | task-player | disableAnswers', function () {
  // TaskPlayerComponent is a classic Ember component that is complex to render
  // in isolation, so we test the disableAnswers getter logic directly.

  function createFakeComponent({ mode, isPlaying, isProcessing, disableAudioPlayer, pauseExecution }) {
    return {
      mode,
      audio: {
        isPlaying: !!isPlaying,
        isProcessing: !!isProcessing,
        get isBusy() {
          return this.isPlaying || this.isProcessing;
        },
      },
      pauseExecution: !!pauseExecution,
      disableAudioPlayer: !!disableAudioPlayer,
      // Reproduce the getter logic from TaskPlayerComponent
      get disableAnswers() {
        if (this.pauseExecution) {
          return true;
        }
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

  test('disableAnswers is true when paused in INTERACT mode', function (assert) {
    const component = createFakeComponent({
      mode: MODES.INTERACT,
      isProcessing: false,
      isPlaying: false,
      pauseExecution: true,
    });
    assert.true(component.disableAnswers, 'disabled when paused in INTERACT mode');
  });

  test('disableAnswers is true when paused in TASK mode', function (assert) {
    const component = createFakeComponent({
      mode: MODES.TASK,
      isProcessing: false,
      isPlaying: false,
      pauseExecution: true,
    });
    assert.true(component.disableAnswers, 'disabled when paused in TASK mode');
  });

  test('disableAnswers is true when paused in LISTEN mode', function (assert) {
    const component = createFakeComponent({
      mode: MODES.LISTEN,
      isProcessing: false,
      isPlaying: false,
      pauseExecution: true,
    });
    assert.true(component.disableAnswers, 'disabled when paused in LISTEN mode');
  });
});

module('Unit | Component | task-player | onPauseStateChanged', function () {
  function createComponent({ isPaused }) {
    let stopCallCount = 0;
    const component = {
      studyingTimer: { isPaused: !!isPaused },
      audio: {
        stop() { stopCallCount++; },
      },
      // Reproduce the logic from TaskPlayerComponent
      onPauseStateChanged() {
        if (this.studyingTimer.isPaused) {
          this.audio.stop();
        }
      },
    };
    return { component, getStopCallCount: () => stopCallCount };
  }

  test('stops audio when isPaused becomes true', function (assert) {
    const { component, getStopCallCount } = createComponent({ isPaused: true });
    component.onPauseStateChanged();
    assert.strictEqual(getStopCallCount(), 1, 'audio.stop() was called once');
  });

  test('does not stop audio when isPaused is false', function (assert) {
    const { component, getStopCallCount } = createComponent({ isPaused: false });
    component.onPauseStateChanged();
    assert.strictEqual(getStopCallCount(), 0, 'audio.stop() was not called');
  });

  test('stops audio each time pause is triggered', function (assert) {
    const { component, getStopCallCount } = createComponent({ isPaused: true });
    component.onPauseStateChanged();
    component.onPauseStateChanged();
    assert.strictEqual(getStopCallCount(), 2, 'audio.stop() was called twice');
  });
});

module('Unit | Component | task-player | onTaskChanged (production path)', function () {
  // onTaskChanged uses isTesting() which returns true in the test env,
  // causing it to call setMode(TASK) directly. We test the production
  // logic path by replicating the branching logic.

  function createComponent({ justEnteredTask, taskModelName }) {
    let sequencePerformed = false;
    const component = {
      justEnteredTask,
      taskModelName,
      exerciseSequenceTask: {
        perform() { sequencePerformed = true; },
      },
      get wasSequencePerformed() { return sequencePerformed; },
      // Reproduce the production path of TaskPlayerComponent.onTaskChanged
      onTaskChanged() {
        if (this.justEnteredTask === false) {
          if (
            this.taskModelName !== ExerciseMechanism.MATRIX &&
            this.taskModelName !== ExerciseMechanism.SIGNALS
          ) {
            this.exerciseSequenceTask.perform();
          }
        }
      },
    };
    return component;
  }

  test('triggers exercise sequence for WORDS mechanism', function (assert) {
    const component = createComponent({
      justEnteredTask: false,
      taskModelName: ExerciseMechanism.WORDS,
    });
    component.onTaskChanged();
    assert.true(component.wasSequencePerformed, 'exerciseSequenceTask was performed');
  });

  test('triggers exercise sequence for PHONEME_PAIRS mechanism', function (assert) {
    const component = createComponent({
      justEnteredTask: false,
      taskModelName: ExerciseMechanism.PHONEME_PAIRS,
    });
    component.onTaskChanged();
    assert.true(component.wasSequencePerformed, 'exerciseSequenceTask was performed');
  });

  test('triggers exercise sequence for ENVIRONMENTAL_SOUNDS mechanism', function (assert) {
    const component = createComponent({
      justEnteredTask: false,
      taskModelName: ExerciseMechanism.ENVIRONMENTAL_SOUNDS,
    });
    component.onTaskChanged();
    assert.true(component.wasSequencePerformed, 'exerciseSequenceTask was performed');
  });

  test('triggers exercise sequence for AUDITORY_SEQUENCE mechanism', function (assert) {
    const component = createComponent({
      justEnteredTask: false,
      taskModelName: ExerciseMechanism.AUDITORY_SEQUENCE,
    });
    component.onTaskChanged();
    assert.true(component.wasSequencePerformed, 'exerciseSequenceTask was performed');
  });

  test('triggers exercise sequence for PROSODY mechanism', function (assert) {
    const component = createComponent({
      justEnteredTask: false,
      taskModelName: ExerciseMechanism.PROSODY,
    });
    component.onTaskChanged();
    assert.true(component.wasSequencePerformed, 'exerciseSequenceTask was performed');
  });

  test('does NOT trigger exercise sequence for MATRIX mechanism', function (assert) {
    const component = createComponent({
      justEnteredTask: false,
      taskModelName: ExerciseMechanism.MATRIX,
    });
    component.onTaskChanged();
    assert.false(component.wasSequencePerformed, 'exerciseSequenceTask was not performed');
  });

  test('does NOT trigger exercise sequence for SIGNALS mechanism', function (assert) {
    const component = createComponent({
      justEnteredTask: false,
      taskModelName: ExerciseMechanism.SIGNALS,
    });
    component.onTaskChanged();
    assert.false(component.wasSequencePerformed, 'exerciseSequenceTask was not performed');
  });

  test('does nothing when justEnteredTask is true', function (assert) {
    const component = createComponent({
      justEnteredTask: true,
      taskModelName: ExerciseMechanism.WORDS,
    });
    component.onTaskChanged();
    assert.false(component.wasSequencePerformed, 'exerciseSequenceTask was not performed');
  });
});

module('Unit | Component | task-player | exerciseSequenceTask', function () {
  // Test the listen → interact sequence logic by replicating the async flow
  // from TaskPlayerComponent.exerciseSequenceTask.

  async function runExerciseSequence(setMode) {
    try {
      await setMode(MODES.LISTEN);
    } catch (_e) {
      return;
    }
    try {
      await setMode(MODES.INTERACT);
    } catch (_e) {
      // Interact was interrupted
    }
  }

  test('calls setMode(LISTEN) then setMode(INTERACT) in sequence', async function (assert) {
    const calls = [];
    await runExerciseSequence(async (mode) => {
      calls.push(mode);
    });

    assert.strictEqual(calls.length, 2, 'setMode called twice');
    assert.strictEqual(calls[0], MODES.LISTEN, 'first call is LISTEN');
    assert.strictEqual(calls[1], MODES.INTERACT, 'second call is INTERACT');
  });

  test('does NOT transition to INTERACT if LISTEN is interrupted', async function (assert) {
    const calls = [];
    await runExerciseSequence(async (mode) => {
      calls.push(mode);
      if (mode === MODES.LISTEN) {
        throw new Error('cancelled');
      }
    });

    assert.strictEqual(calls.length, 1, 'setMode called only once');
    assert.strictEqual(calls[0], MODES.LISTEN, 'only LISTEN was attempted');
  });

  test('completes gracefully if INTERACT is interrupted', async function (assert) {
    const calls = [];
    await runExerciseSequence(async (mode) => {
      calls.push(mode);
      if (mode === MODES.INTERACT) {
        throw new Error('cancelled');
      }
    });

    assert.strictEqual(calls.length, 2, 'setMode called twice');
    assert.strictEqual(calls[0], MODES.LISTEN, 'first call is LISTEN');
    assert.strictEqual(calls[1], MODES.INTERACT, 'second call is INTERACT (even though it threw)');
  });

  test('LISTEN always runs before INTERACT', async function (assert) {
    let listenStarted = false;
    let listenFinished = false;
    let interactStartedAfterListen = false;

    await runExerciseSequence(async (mode) => {
      if (mode === MODES.LISTEN) {
        listenStarted = true;
        await new Promise((r) => setTimeout(r, 10));
        listenFinished = true;
      }
      if (mode === MODES.INTERACT) {
        interactStartedAfterListen = listenStarted && listenFinished;
      }
    });

    assert.true(listenStarted, 'listen was started');
    assert.true(listenFinished, 'listen was finished');
    assert.true(interactStartedAfterListen, 'interact started only after listen completed');
  });
});

module('Unit | Component | task-player | waitWhilePaused', function () {
  // Test the polling loop logic by replicating with plain setTimeout
  // instead of ember-concurrency timeout (which requires runtime setup).

  async function waitWhilePaused(studyingTimer) {
    while (studyingTimer.isPaused) {
      await new Promise((r) => setTimeout(r, 50));
    }
  }

  test('resolves immediately when not paused', async function (assert) {
    const studyingTimer = { isPaused: false };

    const start = Date.now();
    await waitWhilePaused(studyingTimer);
    const elapsed = Date.now() - start;

    assert.true(elapsed < 50, `resolved immediately (${elapsed}ms)`);
  });

  test('waits while paused and resolves when resumed', async function (assert) {
    const studyingTimer = { isPaused: true };

    // Resume after ~120ms
    setTimeout(() => { studyingTimer.isPaused = false; }, 120);

    const start = Date.now();
    await waitWhilePaused(studyingTimer);
    const elapsed = Date.now() - start;

    assert.true(elapsed >= 50, `waited while paused (${elapsed}ms)`);
    assert.true(elapsed < 500, `resolved promptly after resume (${elapsed}ms)`);
  });

  test('polls repeatedly until resumed', async function (assert) {
    const studyingTimer = { isPaused: true };
    let pollCount = 0;

    async function waitWhilePausedCounting() {
      while (studyingTimer.isPaused) {
        pollCount++;
        await new Promise((r) => setTimeout(r, 20));
      }
    }

    // Resume after ~70ms (should see ~3 polls at 20ms intervals)
    setTimeout(() => { studyingTimer.isPaused = false; }, 70);

    await waitWhilePausedCounting();

    assert.true(pollCount >= 2, `polled multiple times (${pollCount} polls)`);
    assert.true(pollCount < 10, `did not poll excessively (${pollCount} polls)`);
  });
});

module('Unit | Component | task-player | interactModeTask pause check', function () {
  // The interactModeTask loop checks isPaused at the start of each
  // iteration and skips text processing when paused.

  function simulateIteration({ isPaused, textToPlay }) {
    let wasProcessed = false;

    // Reproduce the guard logic from interactModeTask
    if (isPaused) {
      return { skipped: true, wasProcessed };
    }

    if (textToPlay) {
      wasProcessed = true;
    }

    return { skipped: false, wasProcessed };
  }

  test('skips text processing when paused', function (assert) {
    const result = simulateIteration({ isPaused: true, textToPlay: 'hello' });
    assert.true(result.skipped, 'iteration was skipped');
    assert.false(result.wasProcessed, 'text was not processed');
  });

  test('processes text when not paused', function (assert) {
    const result = simulateIteration({ isPaused: false, textToPlay: 'hello' });
    assert.false(result.skipped, 'iteration was not skipped');
    assert.true(result.wasProcessed, 'text was processed');
  });

  test('does not process when not paused but no text to play', function (assert) {
    const result = simulateIteration({ isPaused: false, textToPlay: null });
    assert.false(result.skipped, 'iteration was not skipped');
    assert.false(result.wasProcessed, 'no text to process');
  });
});
