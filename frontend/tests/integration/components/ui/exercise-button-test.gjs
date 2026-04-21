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

  test('tooltip points at the available key when the exercise is available', async function (assert) {
    this.setProperties({ exercise: { id: '1', level: 1, isCompleted: false } });
    const self = this;

    await render(
      <template>
        <UiExerciseButton
          @exercise={{self.exercise}}
          @isAvailable={{true}}
        />
      </template>,
    );

    assert.dom('.c-exercise-button').hasAttribute(
      'title',
      'Click to start this task',
    );
  });

  test('tooltip points at the locked key when the exercise is not available', async function (assert) {
    this.setProperties({ exercise: { id: '1', level: 1, isCompleted: false } });
    const self = this;

    await render(
      <template>
        <UiExerciseButton
          @exercise={{self.exercise}}
          @isAvailable={{false}}
        />
      </template>,
    );

    assert.dom('.c-exercise-button').hasAttribute(
      'title',
      'This task is not available yet. It will open once you finish the previous one.',
    );
  });
});
