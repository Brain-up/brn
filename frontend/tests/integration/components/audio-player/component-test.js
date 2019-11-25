import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './page-object';
import { timeout } from 'ember-concurrency';

module('Integration | Component | audio-player', function(hooks) {
  setupRenderingTest(hooks);

  test('it disables button when playing', async function(assert) {
    await render(hbs`<AudioPlayer @audioFileUrl="/audio/no_noise/бал.mp3"/>`);

    const audioElement = document.querySelector('[data-test-audio-player]');
    audioElement.muted = true;
    audioElement.autoplay = false;

    await timeout(1000);

    assert.dom('[data-test-play-audio-button]').isNotDisabled();

    await pageObject.playAudio();

    await timeout(100);

    assert.dom('[data-test-play-audio-button]').isDisabled();

    await timeout(audioElement.duration * 1000 + 1000);

    assert.dom('[data-test-play-audio-button]').isNotDisabled();
  });
});
