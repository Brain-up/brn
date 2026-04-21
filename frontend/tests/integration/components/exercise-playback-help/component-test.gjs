import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import ExercisePlaybackHelp from 'brn/components/exercise-playback-help';

module('Integration | Component | exercise-playback-help', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('the icon trigger is rendered; dialog is closed by default', async function (assert) {
    await render(<template><ExercisePlaybackHelp /></template>);

    assert.dom('[data-test-playback-help-trigger]').exists();
    assert.dom('[data-test-instructions-dialog]').doesNotExist();
  });

  test('clicking the trigger opens the playback help dialog', async function (assert) {
    await render(<template><ExercisePlaybackHelp /></template>);

    await click('[data-test-playback-help-trigger]');

    assert.dom('[data-test-instructions-dialog]').exists();
    assert
      .dom('[data-test-instructions-dialog-title]')
      .hasText('t:instructions.playback_title');
    assert.dom('[data-test-playback-help-body]').exists();
  });

  test('the close button dismisses the dialog', async function (assert) {
    await render(<template><ExercisePlaybackHelp /></template>);

    await click('[data-test-playback-help-trigger]');
    await click('[data-test-instructions-dialog-close]');

    assert.dom('[data-test-instructions-dialog]').doesNotExist();
  });
});
