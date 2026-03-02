import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { timeout } from 'ember-concurrency';
import pageObject from './page-object';
import { TIMINGS } from 'brn/utils/audio-api';
import customTimeout from 'brn/utils/custom-timeout';
import Service from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import AudioPlayer from 'brn/components/audio-player';

module('Integration | Component | audio-player', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  hooks.beforeEach(async function () {
    const fakeAudio = {
      currentTime: 0,
      duration: 60,
    };

    this.set('audioElements', [{ ...fakeAudio }, { ...fakeAudio }]);
    this.set('emptyList', []);
  });

  test('it registers itself to audio service', async function (assert) {
    assert.expect(1);
    class MockAudio extends Service {
      register(ctx) {
        assert.ok(ctx);
      }
      stop() {
        assert.ok(true);
      }
    }
    this.owner.register('service:audio', MockAudio);

    const self = this;




    await render(<template><AudioPlayer
    @audioElements={{self.audioElements}}
    /></template>);
  });

  test('it disables button when playing', async function (assert) {
    const self = this;




    await render(<template><AudioPlayer
    @audioElements={{self.audioElements}}
    /></template>);
    assert.dom('[data-test-play-audio-button]').isNotDisabled();

    pageObject.playAudio();

    await timeout(TIMINGS.FAKE_AUDIO_STARTED);

    assert.dom('[data-test-play-audio-button]').isDisabled();

    await timeout(TIMINGS.FAKE_AUDIO_FINISHED);

    assert.dom('[data-test-play-audio-button]').isNotDisabled();
  });

  test('it shows playing progress', async function (assert) {
    const self = this;




    await render(<template><AudioPlayer
    @audioElements={{self.audioElements}}
    /></template>);
    pageObject.playAudio();

    await customTimeout();

    assert
      .dom('[data-test-play-audio-button]')
      .hasAttribute('data-test-playing-progress', '100');
  });

  test('it disables button when audio is loading (isBusy)', async function (assert) {
    const audioService = this.owner.lookup('service:audio');

    const self = this;




    await render(<template><AudioPlayer
    @audioElements={{self.audioElements}}
    /></template>);

    assert.dom('[data-test-play-audio-button]').isNotDisabled('button enabled when idle');

    audioService.isProcessing = true;

    await customTimeout();

    assert.dom('[data-test-play-audio-button]').isDisabled('button disabled when isProcessing is true');

    audioService.isProcessing = false;

    await customTimeout();

    assert.dom('[data-test-play-audio-button]').isNotDisabled('button re-enabled when isProcessing clears');
  });

  test('clicking play button while isProcessing does not trigger startPlayTask', async function (assert) {
    let setAudioElementsCalled = false;

    class MockAudio extends Service {
      @tracked isPlaying = false;
      @tracked isProcessing = false;
      @tracked audioPlayingProgress = 0;
      get isBusy() {
        return this.isPlaying || this.isProcessing;
      }
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      register() {}
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      stop() {}
      startPlayTask() {
        if (this.isBusy) {
          return;
        }
        setAudioElementsCalled = true;
      }
    }
    this.owner.register('service:audio', MockAudio);
    const audioService = this.owner.lookup('service:audio');

    const self = this;




    await render(<template><AudioPlayer
    @audioElements={{self.audioElements}}
    /></template>);

    // Set isProcessing to simulate audio loading in progress
    audioService.isProcessing = true;

    await customTimeout();

    // Button should be disabled (component's isPlaying getter returns isBusy)
    assert.dom('[data-test-play-audio-button]').isDisabled('button is disabled during processing');

    // Even if someone managed to call startPlayTask directly, isBusy guard prevents execution
    audioService.startPlayTask();
    assert.false(setAudioElementsCalled, 'startPlayTask does not proceed when isBusy is true');

    // Clean up
    audioService.isProcessing = false;
  });
});
