import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | exercise-stats', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    this.set('stats', {
      endTime: new Date(1000),
      startTime: new Date(100),
      rightAnswersCount: 10,
      repeatsCount: 12,
      wrongAnswersCount: 10,
    });

    this.set('onComplete', () => {
      assert.ok('Completed');
    });

    await render(
      hbs`<ExerciseStats @stats={{this.stats}} @onComplete={{this.onComplete}} />`,
    );

    assert.dom('[data-test-exercise-stats]').exists();
    assert.dom('[data-test-continue]').exists();
    await click('[data-test-continue]');
  });
});
