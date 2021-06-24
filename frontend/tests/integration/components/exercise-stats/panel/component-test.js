import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | exercise-stats/panel', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(
      hbs`<ExerciseStats::Panel @type="positive" @label="Foo" @value="42"/>`,
    );

    assert.dom('[data-test-type="positive"]').exists();
    assert.dom('[data-test-label]').hasText('Foo');
    assert.dom('[data-test-value]').hasText('42');

    await render(
      hbs`<ExerciseStats::Panel @type="total" @label="Foo" @value="42"  @totalStat={{true}}/>`,
    );
    assert.dom('[data-test-type="total"]').exists();
    assert.dom('[data-test-label]').hasText('Foo');
    assert.dom('[data-test-value]').hasText('42');
  });

  test('it support bock invocation', async function (assert) {
    await render(
      hbs`<ExerciseStats::Panel @type="positive" @label="Foo">42</ExerciseStats::Panel>`,
    );
    assert.dom('[data-test-value]').hasText('42');
  });
});
