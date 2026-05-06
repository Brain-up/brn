import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, settled } from '@ember/test-helpers';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import ExerciseSteps from 'brn/components/exercise-steps';

// Helper: walk through LISTEN → INTERACT so `setLastMode` populates
// the component's internal `modes` array with both entries. Without
// this, modeForTask stays DISABLED regardless of interactReady and
// STATE_LOCKED wins in taskBtnClass.
class StepState {
  @tracked step = 'listen';
  @tracked ready = false;
}

async function walkToInteract(state) {
  state.step = 'interact';
  await settled();
}

module('Integration | Component | exercise-steps', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders three step buttons', async function (assert) {
    await render(<template><ExerciseSteps /></template>);

    assert.dom('button').exists({ count: 3 });
  });

  test('solve button gets the "next" style once interactReady is true during Interact', async function (assert) {
    const state = new StepState();
    await render(
      <template>
        <ExerciseSteps
          @activeStep={{state.step}}
          @visible={{true}}
          @interactReady={{state.ready}}
        />
      </template>,
    );

    await walkToInteract(state);
    state.ready = true;
    await settled();

    assert
      .dom('button:nth-of-type(3)')
      .hasClass('exercise-step-btn--next', 'solve button lights up as next step');
  });

  test('solve button stays default when interactReady is false during Interact', async function (assert) {
    const state = new StepState();
    await render(
      <template>
        <ExerciseSteps
          @activeStep={{state.step}}
          @visible={{true}}
          @interactReady={{state.ready}}
        />
      </template>,
    );

    await walkToInteract(state);

    assert
      .dom('button:nth-of-type(3)')
      .doesNotHaveClass(
        'exercise-step-btn--next',
        'solve button is not highlighted yet',
      );
  });

  test('solve button exposes the ready-when-you-are hint via title', async function (assert) {
    // ember-intl in this test env returns the `t:<key>` placeholder when
    // the translation is not loaded (see doctor-feedback/component-test.gjs
    // for the same pattern). We just verify the hint key is wired up.
    await render(
      <template>
        <ExerciseSteps @activeStep="interact" @visible={{true}} />
      </template>,
    );

    assert
      .dom('button:nth-of-type(3)')
      .hasAttribute(
        'title',
        't:control_exercises.solve_hint',
        'solve button binds the hint translation key',
      );
  });
});
