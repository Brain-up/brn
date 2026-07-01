import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import AudioService from 'brn/services/audio';

module('Unit | Service | audio', function (hooks) {
  setupTest(hooks);

  test('registers objects', function (assert) {
    const testObject = {
      a: 1,
      testFunc() {
        assert.ok(true, "can call registered object's methods");
      },
    };
    let service = this.owner.lookup('service:audio');
    service.register(testObject);

    service.player.testFunc();
  });

  test('isBusy is false when idle', function (assert) {
    let service = this.owner.lookup('service:audio');
    assert.false(service.isBusy, 'isBusy is false when neither playing nor loading');
  });

  test('isBusy is true when isPlaying', function (assert) {
    let service = this.owner.lookup('service:audio');
    service.isPlaying = true;
    assert.true(service.isBusy, 'isBusy is true when isPlaying');
  });

  test('isBusy is true when isProcessing', function (assert) {
    let service = this.owner.lookup('service:audio');
    service.isProcessing = true;
    assert.true(service.isBusy, 'isBusy is true when isProcessing');
  });

  test('startPlayTask sets isProcessing during execution', async function (assert) {
    const loadingStates = [];

    class TestAudioService extends AudioService {
      async setAudioElements() {
        loadingStates.push(this.isProcessing);
      }
      async playAudio() {
        loadingStates.push(this.isProcessing);
      }
    }
    this.owner.register('service:audio', TestAudioService);

    let service = this.owner.lookup('service:audio');
    await service.startPlayTask(['http://example.com/audio.mp3']);

    assert.true(loadingStates[0], 'isProcessing is true during setAudioElements');
    assert.true(loadingStates[1], 'isProcessing is true during playAudio');
    assert.false(service.isProcessing, 'isProcessing is false after completion');
  });

  test('startPlayTask resets isProcessing on error', async function (assert) {
    class TestAudioService extends AudioService {
      async setAudioElements() {
        throw new Error('network error');
      }
    }
    this.owner.register('service:audio', TestAudioService);

    let service = this.owner.lookup('service:audio');
    try {
      await service.startPlayTask(['http://example.com/audio.mp3']);
    } catch (_e) {
      // expected
    }

    assert.false(service.isProcessing, 'isProcessing is reset after error');
  });

  test('startPlayTask is blocked when isBusy', async function (assert) {
    let callCount = 0;

    class TestAudioService extends AudioService {
      async setAudioElements() {
        callCount++;
      }
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      async playAudio() {}
    }
    this.owner.register('service:audio', TestAudioService);

    let service = this.owner.lookup('service:audio');

    // Simulate already loading
    service.isProcessing = true;
    await service.startPlayTask(['http://example.com/audio.mp3']);
    assert.strictEqual(callCount, 0, 'does not proceed when isProcessing is true');

    // Simulate already playing
    service.isProcessing = false;
    service.isPlaying = true;
    await service.startPlayTask(['http://example.com/audio.mp3']);
    assert.strictEqual(callCount, 0, 'does not proceed when isPlaying is true');

    // Normal case
    service.isPlaying = false;
    await service.startPlayTask(['http://example.com/audio.mp3']);
    assert.strictEqual(callCount, 1, 'proceeds when idle');
  });

  test('setAudioElements reuses existing AudioContext', async function (assert) {
    let service = this.owner.lookup('service:audio');

    // First call creates a context
    await service.setAudioElements(['http://example.com/audio.mp3']);
    const firstContext = service.context;
    assert.ok(firstContext, 'context is created on first call');

    // Stub resume to prevent Chrome's suspended-state hang in tests
    firstContext.resume = () => Promise.resolve();

    // Second call reuses the same context
    await service.setAudioElements(['http://example.com/audio2.mp3']);
    const secondContext = service.context;
    assert.strictEqual(secondContext, firstContext, 'context is reused on second call');
  });

  test('setAudioElements creates new context if previous was closed', async function (assert) {
    let service = this.owner.lookup('service:audio');

    await service.setAudioElements(['http://example.com/audio.mp3']);
    const firstContext = service.context;

    await firstContext.close();
    assert.strictEqual(firstContext.state, 'closed', 'context is closed');

    await service.setAudioElements(['http://example.com/audio2.mp3']);
    const secondContext = service.context;
    assert.notStrictEqual(secondContext, firstContext, 'new context is created when old one was closed');
    assert.notStrictEqual(secondContext.state, 'closed', 'new context is not closed');
  });

  test('startPlayTask finally block guards against destroyed service', async function (assert) {
    let resolveSetAudioElements;

    class TestAudioService extends AudioService {
      setAudioElements() {
        return new Promise((resolve) => {
          resolveSetAudioElements = resolve;
        });
      }
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      async playAudio() {}
    }
    this.owner.register('service:audio', TestAudioService);

    let service = this.owner.lookup('service:audio');

    // Start the task but don't await - it will block on setAudioElements
    const taskPromise = service.startPlayTask(['http://example.com/audio.mp3']);
    assert.true(service.isProcessing, 'isProcessing is true while waiting');

    // Simulate service destruction while startPlayTask is in-flight
    service.willDestroy();

    // Now let the async work complete after destruction
    resolveSetAudioElements();
    await taskPromise;

    // The guard should have prevented setting isProcessing on the destroyed service.
    // If the guard were missing, Ember would throw an error on setting a tracked
    // property on a destroyed object. The fact we get here without error is the assertion.
    assert.ok(true, 'no error thrown when finally block runs on destroyed service');
  });

  test('willDestroy closes the AudioContext', async function (assert) {
    let service = this.owner.lookup('service:audio');

    await service.setAudioElements(['http://example.com/audio.mp3']);
    const ctx = service.context;
    assert.notStrictEqual(ctx.state, 'closed', 'context is open before destroy');

    service.willDestroy();

    assert.strictEqual(ctx.state, 'closed', 'context is closed after destroy');
  });

  module('playTask onended handling', function () {
    // Build a plain object that satisfies `rawSource instanceof
    // AudioBufferSourceNode` via prototype injection. Avoids real
    // AudioContext, which in headless CI is created in the suspended
    // state and makes stop()/onended timing unreliable.
    function fakeNativeSource(durationSec, options = {}) {
      const { autoEndAfterMs = null, suppressOnended = false } = options;
      const source = {
        buffer: { duration: durationSec },
        _onended: null,
        get onended() {
          return this._onended;
        },
        set onended(fn) {
          if (suppressOnended) return;
          this._onended = fn;
        },
        start() {
          if (autoEndAfterMs !== null) {
            setTimeout(() => this._onended && this._onended(), autoEndAfterMs);
          }
        },
        stop() {
          if (this._onended) this._onended();
        },
      };
      Object.setPrototypeOf(source, AudioBufferSourceNode.prototype);
      return source;
    }

    function stubContext(service) {
      service.context = {
        state: 'running',
        resume: () => Promise.resolve(),
        close: () => Promise.resolve(),
      };
    }

    test('awaits onended and advances on the event, not a wall-clock timer', async function (assert) {
      const service = this.owner.lookup('service:audio');
      stubContext(service);
      // 5-second nominal duration, onended fires after 50ms. A timer-only
      // loop would have waited ~5s; onended should release it in ~50ms.
      const source = fakeNativeSource(5, { autoEndAfterMs: 50 });
      service.createSources = async () => [{ source, gainNode: {} }];
      service.buffers = [{}];

      const t0 = Date.now();
      await service.playTask.perform();
      const elapsed = Date.now() - t0;

      assert.ok(source instanceof AudioBufferSourceNode, 'prototype satisfies instanceof');
      assert.true(
        elapsed < 2000,
        `advanced on onended at ~50ms, not wall-clock 5s (${elapsed}ms)`,
      );
      assert.false(service.isPlaying, 'isPlaying is reset after completion');
    });

    test('falls back to duration + 1s when onended never fires', async function (assert) {
      const service = this.owner.lookup('service:audio');
      stubContext(service);
      // 50ms duration, onended assignment is swallowed → safety net must
      // release after duration + 1000ms. Assert against the full 1050 so
      // shrinking the margin in the future is caught.
      const source = fakeNativeSource(0.05, { suppressOnended: true });
      service.createSources = async () => [{ source, gainNode: {} }];
      service.buffers = [{}];

      const t0 = Date.now();
      await service.playTask.perform();
      const elapsed = Date.now() - t0;

      assert.true(
        elapsed >= 1050,
        `safety net waited at least duration + 1s (${elapsed}ms)`,
      );
      assert.true(
        elapsed < 3000,
        `safety net did not hang (${elapsed}ms)`,
      );
    });

    test('swallowed synchronous source.start error does not strand the loop', async function (assert) {
      const service = this.owner.lookup('service:audio');
      stubContext(service);
      const source = fakeNativeSource(5);
      source.start = () => {
        throw new Error('closed context');
      };
      service.createSources = async () => [{ source, gainNode: {} }];
      service.buffers = [{}];

      const t0 = Date.now();
      await service.playTask.perform();
      const elapsed = Date.now() - t0;

      // If the start-failure branch were absent, the loop would hang on
      // the safety-net timeout (≥1050ms). It should exit nearly instantly.
      assert.true(
        elapsed < 500,
        `loop bails out on sync start throw (${elapsed}ms)`,
      );
    });
  });

  module('startNoiseTask', function () {
    test('does not create or start noise when the exercise has no noise level', async function (assert) {
      class TestAudioService extends AudioService {
        get currentExerciseNoiseLevel() {
          return 0;
        }
        get currentExerciseNoiseUrl() {
          return null;
        }
        async getNoise() {
          assert.step('getNoise');
          return { source: { start() {}, stop() {} }, gainNode: {} };
        }
      }
      this.owner.register('service:audio', TestAudioService);
      const service = this.owner.lookup('service:audio');

      await service.startNoiseTask.perform();

      assert.verifySteps([], 'no noise is created when the level is 0');
    });

    test('creates and starts the noise source when the exercise has a noise level', async function (assert) {
      class TestAudioService extends AudioService {
        get currentExerciseNoiseLevel() {
          return 50;
        }
        get currentExerciseNoiseUrl() {
          return 'http://example.com/noise.mp3';
        }
        async getNoise() {
          assert.step('getNoise');
          return {
            source: {
              start() {
                assert.step('start');
              },
              stop() {
                assert.step('stop');
              },
            },
            gainNode: {},
          };
        }
      }
      this.owner.register('service:audio', TestAudioService);
      const service = this.owner.lookup('service:audio');
      // A running context is the post-restart case; the suspended-context
      // resume is production-only (guarded by !isTesting()).
      service.context = { state: 'running', resume: () => Promise.resolve() };

      const instance = service.startNoiseTask.perform();
      // The task loops on timeout(6000) after starting; cancel once started.
      await new Promise((resolve) => setTimeout(resolve, 20));
      instance.cancel();
      try {
        await instance;
      } catch (_e) {
        // cancellation is expected
      }

      // Ordered: the source is created, then started, then stopped on cancel.
      assert.verifySteps(
        ['getNoise', 'start', 'stop'],
        'noise is created before it is started, and stopped on cancellation',
      );
    });
  });
});
