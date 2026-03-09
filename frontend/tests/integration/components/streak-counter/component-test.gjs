import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import StreakCounter from 'brn/components/streak-counter';

module('Integration | Component | streak-counter', function (hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(function () {
    localStorage.removeItem('brn-gamification');
  });

  hooks.afterEach(function () {
    localStorage.removeItem('brn-gamification');
  });

  test('hidden when streak is 0', async function (assert) {
    await render(<template><StreakCounter /></template>);
    assert.dom('[data-test-streak-counter]').doesNotExist();
  });

  test('shows when streak > 0 after completing an exercise', async function (assert) {
    const gamification = this.owner.lookup('service:gamification');
    gamification.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    await render(<template><StreakCounter /></template>);
    assert.dom('[data-test-streak-counter]').exists();
  });

  test('displays current streak count', async function (assert) {
    const gamification = this.owner.lookup('service:gamification');
    gamification.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    await render(<template><StreakCounter /></template>);
    assert.dom('.streak-counter__text').hasText('1');
  });

  test('displays higher streak count from persisted state', async function (assert) {
    const today = new Date().toISOString().split('T')[0];

    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 500,
        currentStreak: 7,
        longestStreak: 7,
        lastActiveDate: today,
        exercisesCompleted: 10,
        perfectExercises: 5,
        badges: {},
      }),
    );

    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );

    await render(<template><StreakCounter /></template>);
    assert.dom('[data-test-streak-counter]').exists();
    assert.dom('.streak-counter__text').hasText('7');
  });

  test('hidden when persisted streak is 0', async function (assert) {
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 100,
        currentStreak: 0,
        longestStreak: 5,
        lastActiveDate: '2025-01-10',
        exercisesCompleted: 5,
        perfectExercises: 2,
        badges: {},
      }),
    );

    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );

    await render(<template><StreakCounter /></template>);
    assert.dom('[data-test-streak-counter]').doesNotExist();
  });
});
