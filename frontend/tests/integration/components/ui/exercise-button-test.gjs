import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import UiExerciseButton from 'brn/components/ui/exercise-button';

module('Integration | Component | ui/exercise-button', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    this.setProperties({
      exercise: { isCompleted: true },
    });

    const self = this;




    await render(<template><UiExerciseButton @exercise={{self.exercise}} /></template>);

    assert.dom('.completed').exists();
  });
});
