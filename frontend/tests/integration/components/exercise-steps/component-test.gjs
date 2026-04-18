import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import ExerciseSteps from 'brn/components/exercise-steps';

module('Integration | Component | exercise-steps', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders three step buttons', async function (assert) {
    await render(<template><ExerciseSteps /></template>);

    assert.dom('button').exists({ count: 3 });
  });

  test('solve button gets the "next" style when interactReady is true while in Interact', async function (assert) {
    await render(
      <template>
        <ExerciseSteps
          @activeStep="interact"
          @visible={{true}}
          @interactReady={{true}}
        />
      </template>,
    );

    assert.dom('button:nth-of-type(3)').hasClass(
      'exercise-step-btn--next',
      'solve button lights up as next step',
    );
  });

  test('solve button stays default when interactReady is false during Interact', async function (assert) {
    await render(
      <template>
        <ExerciseSteps
          @activeStep="interact"
          @visible={{true}}
          @interactReady={{false}}
        />
      </template>,
    );

    assert.dom('button:nth-of-type(3)').doesNotHaveClass(
      'exercise-step-btn--next',
      'solve button is not highlighted yet',
    );
  });

  test('solve button exposes a hint via title attribute', async function (assert) {
    await render(
      <template>
        <ExerciseSteps @activeStep="interact" @visible={{true}} />
      </template>,
    );

    assert
      .dom('button:nth-of-type(3)')
      .hasAttribute(
        'title',
        'Click when you are ready to start solving',
        'solve button shows the ready-when-you-are hint',
      );
  });
});
