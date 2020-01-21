import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './page-object';
import customTimeout from 'brn/utils/custom-timeout';
import AudioPlayer, {
  createAnimationInterval,
} from 'brn/components/audio-player/component';

AudioPlayer.reopen({
  async playAudio() {
    this.isDestroyed ? '' : this.set('isPlaying', true);
    await customTimeout();
    createAnimationInterval.apply(this);
    await customTimeout();
    this.isDestroyed ? '' : this.set('isPlaying', false);
  },
});

module('Integration | Component | audio-player', function(hooks) {
  setupRenderingTest(hooks);

  test('it disables button when playing', async function(assert) {
    await render(hbs`<AudioPlayer/>`);

    assert.dom('[data-test-play-audio-button]').isNotDisabled();

    pageObject.playAudio();

    await customTimeout();
    await customTimeout();

    assert.dom('[data-test-play-audio-button]').isDisabled();

    await customTimeout();

    assert.dom('[data-test-play-audio-button]').isNotDisabled();
  });

  test('it shows playing progress', async function(assert) {
    const fakeAudio = {
      currentTime: 30,
      duration: 60,
    };

    this.set('audioElements', [fakeAudio]);
    this.set('setAudioElements', () => {});
    this.set('emptyList', []);

    await render(
      hbs`<AudioPlayer
        @audioElements={{this.audioElements}}
        @setAudioElements={{this.setAudioElements}}
        @autoplay={{true}}
        @previousPlayedUrls={{this.emptyList}}
      />`,
    );

    await customTimeout();
    await customTimeout();

    assert
      .dom('[data-test-play-audio-button]')
      .hasAttribute('data-test-playing-progress', '50');
  });
});
