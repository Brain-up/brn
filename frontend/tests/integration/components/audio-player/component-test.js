import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './page-object';
import customTimeout from 'brn/utils/custom-timeout';
import AudioPlayer from 'brn/components/audio-player/component';

AudioPlayer.reopen({
  async playAudio() {
    this.isDestroyed ? '' : this.set('isPlaying', true);
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

    assert.dom('[data-test-play-audio-button]').isDisabled();

    await customTimeout();

    assert.dom('[data-test-play-audio-button]').isNotDisabled();
  });
});
