import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import ExerciseType from 'brn/components/exercise-type';

module('Integration | Component | exercise-type', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><ExerciseType @initial="foo" /></template>);

    assert.ok(this.element.textContent.trim(), 'foo');
  });
});
