import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click, fillIn } from '@ember/test-helpers';
import ExerciseStudyConfig from 'brn/components/exercise-study-config';

module('Integration | Component | exercise-study-config', function(hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  hooks.afterEach(function () {
    localStorage.removeItem('audioPlaybackRate');
  });

  test('it renders', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    const controller = this.owner.lookup('controller:group.series.subgroup.exercise.task');

    controller.set('model', {
      shouldBeWithPictures: true,
    });

    await render(<template><ExerciseStudyConfig /></template>);

    assert.dom('button').exists();

    await click('[data-test-toggle-image-visibility]');

    assert.dom('button').exists();
  });

  test('it not renders without flag', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    const controller = this.owner.lookup('controller:group.series.subgroup.exercise.task');

    controller.set('model', {
      shouldBeWithPictures: false,
    });

    assert.dom('button').doesNotExist();
  });

  test('it renders a speech-rate selector that updates the user preference', async function (assert) {
    const userData = this.owner.lookup('service:user-data');
    userData.setAudioPlaybackRate(1);

    await render(<template><ExerciseStudyConfig /></template>);

    assert.dom('[data-test-speech-rate]').exists('speech-rate selector is shown');
    assert.dom('[data-test-speech-rate]').hasValue('1', 'reflects the current preference');

    await fillIn('[data-test-speech-rate]', '0.5');

    assert.strictEqual(
      userData.audioPlaybackRate,
      0.5,
      'selecting a rate updates the user preference',
    );
  });
});
