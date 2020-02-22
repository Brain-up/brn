import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import { timeout } from 'ember-concurrency';
import pageObject from './page-object';
import customTimeout from 'brn/utils/custom-timeout';

module('Integration | Component | audio-player', function(hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function() {
    const fakeAudio = {
      currentTime: 0,
      duration: 60,
    };

    this.set('audioElements', [{ ...fakeAudio }, { ...fakeAudio }]);
    this.set('setAudioElements', () => {});
    this.set('emptyList', []);

    await render(
      hbs`<AudioPlayer
        @audioElements={{this.audioElements}}
        @setAudioElements={{this.setAudioElements}}
        @previousPlayedUrls={{this.emptyList}}
      />`,
    );
  });

  test('it disables button when playing', async function(assert) {
    assert.dom('[data-test-play-audio-button]').isNotDisabled();

    pageObject.playAudio();

    await timeout(5);

    assert.dom('[data-test-play-audio-button]').isDisabled();

    await timeout(20);

    assert.dom('[data-test-play-audio-button]').isNotDisabled();
  });

  test('it shows playing progress', async function(assert) {
    pageObject.playAudio();

    await customTimeout();

    assert
      .dom('[data-test-play-audio-button]')
      .hasAttribute('data-test-playing-progress', '100');
  });
});
