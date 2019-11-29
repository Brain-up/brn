import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './page-object';
import { timeout } from 'ember-concurrency';
import AudioPlayer from 'brn/components/audio-player/component';

AudioPlayer.reopen({
  actions: {
    async playAudio() {
      this.set('isPlaying', true);

      await timeout(1000);

      this.set('isPlaying', false);
    },
  },
});

module('Integration | Component | audio-player', function(hooks) {
  setupRenderingTest(hooks);

  test('it disables button when playing', async function(assert) {
    await render(hbs`<AudioPlayer/>`);

    assert.dom('[data-test-play-audio-button]').isNotDisabled();

    pageObject.playAudio();

    await timeout(500);

    assert.dom('[data-test-play-audio-button]').isDisabled();

    await timeout(1000);

    assert.dom('[data-test-play-audio-button]').isNotDisabled();
  });
});
