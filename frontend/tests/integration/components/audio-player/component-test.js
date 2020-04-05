import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import { timeout } from 'ember-concurrency';
import pageObject from './page-object';
import { TIMINGS } from 'brn/utils/audio-api';
import customTimeout from 'brn/utils/custom-timeout';
import Service from '@ember/service';

module('Integration | Component | audio-player', function(hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function() {
    const fakeAudio = {
      currentTime: 0,
      duration: 60,
    };

    this.set('audioElements', [{ ...fakeAudio }, { ...fakeAudio }]);
    this.set('emptyList', []);
  });

  test('it registers itself to audio service', async function(assert) {
    assert.expect(1);
    class MockAudio extends Service {
      register(ctx) {
        assert.ok(ctx);
      }
    }
    this.owner.register('service:audio', MockAudio);

    await render(
      hbs`
      <AudioPlayer
        @audioElements={{this.audioElements}}
      />`,
    );
  });

  test('it disables button when playing', async function(assert) {
    await render(
      hbs`<AudioPlayer
        @audioElements={{this.audioElements}}
      />`,
    );
    assert.dom('[data-test-play-audio-button]').isNotDisabled();

    pageObject.playAudio();

    await timeout(TIMINGS.FAKE_AUDIO_STARTED);

    assert.dom('[data-test-play-audio-button]').isDisabled();

    await timeout(TIMINGS.FAKE_AUDIO_FINISHED);

    assert.dom('[data-test-play-audio-button]').isNotDisabled();
  });

  test('it shows playing progress', async function(assert) {
    await render(
      hbs`<AudioPlayer
        @audioElements={{this.audioElements}}
      />`,
    );
    pageObject.playAudio();

    await customTimeout();

    assert
      .dom('[data-test-play-audio-button]')
      .hasAttribute('data-test-playing-progress', '100');
  });
});
