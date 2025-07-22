import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | exercise-study-config', function(hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    const controller = this.owner.lookup('controller:group.series.subgroup.exercise.task');

    controller.set('model', {
      shouldBeWithPictures: true,
    });

    await render(hbs`<ExerciseStudyConfig />`);

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
});
