import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import ExerciseStepsStep from 'brn/components/exercise-steps/step';

module('Integration | Component | exercise-steps/step', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><ExerciseStepsStep /></template>);

    assert.equal(this.element.textContent.trim(), '');

    // Template block usage:
    await render(<template><ExerciseStepsStep>
    template block text
    </ExerciseStepsStep></template>);

    assert.equal(this.element.textContent.trim(), 'template block text');
  });
});
