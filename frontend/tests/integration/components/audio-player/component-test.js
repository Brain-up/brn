import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';
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
    assert.notOk(pageObject.buttonIsDisabled, 'button is not disabled');

    pageObject.playAudio();

    await customTimeout();

    assert.ok(pageObject.buttonIsDisabled, 'button is disabled');

    await customTimeout();
    await customTimeout();

    assert.notOk(pageObject.buttonIsDisabled, 'button is not disabled');
  });

  test('it shows playing progress', async function(assert) {
    pageObject.playAudio();

    await customTimeout();

    assert.equal(pageObject.progressValue, '100', 'progress value is 100');
  });
});
