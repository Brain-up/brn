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
    } catch (e) {
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
});
