import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import ExerciseStatsPanel from 'brn/components/exercise-stats/panel';

module('Integration | Component | exercise-stats/panel', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(
        <template><ExerciseStatsPanel @type="positive" @label="Foo" @value="42"/></template>
    );

    assert.dom('[data-test-type="positive"]').exists();
    assert.dom('[data-test-label]').hasText('Foo');
    assert.dom('[data-test-value]').hasText('42');

    await render(
        <template><ExerciseStatsPanel @type="total" @label="Foo" @value="42"  @totalStat={{true}}/></template>
    );
    assert.dom('[data-test-type="total"]').exists();
    assert.dom('[data-test-label]').hasText('Foo');
    assert.dom('[data-test-value]').hasText('42');
  });

  test('it support bock invocation', async function (assert) {
    await render(
        <template><ExerciseStatsPanel @type="positive" @label="Foo">42</ExerciseStatsPanel></template>
    );
    assert.dom('[data-test-value]').hasText('42');
  });
});
