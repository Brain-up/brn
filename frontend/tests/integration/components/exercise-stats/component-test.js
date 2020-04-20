import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | exercise-stats', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {

    this.set('stats', {
      endTime: new Date(1000),
      startTime: new Date(100),
      repetitionIndex: 1.66
    });

    await render(hbs`<ExerciseStats @stats={{this.stats}} />`);

    assert.dom('[data-test-exercise-stats]').exists();
    assert.dom('[data-test-type="negative"]').exists();
    assert.dom('[data-test-type="positive"]').exists();
    assert.dom('[data-test-type="neutral"]').exists();
  });
});
