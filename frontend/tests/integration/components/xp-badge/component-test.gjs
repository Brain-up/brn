import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { setupIntl } from 'ember-intl/test-support';
import XpBadge from 'brn/components/xp-badge';

module('Integration | Component | xp-badge', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  hooks.beforeEach(function () {
    localStorage.removeItem('brn-gamification');
  });

  hooks.afterEach(function () {
    localStorage.removeItem('brn-gamification');
  });

  test('it renders the xp-badge element', async function (assert) {
    await render(<template><XpBadge /></template>);
    assert.dom('[data-test-xp-badge]').exists();
  });

  test('it renders the SVG ring', async function (assert) {
    await render(<template><XpBadge /></template>);
    assert.dom('.xp-badge__ring').exists('SVG ring is rendered');
    assert.dom('.xp-badge__ring circle').exists('SVG circles are rendered');
  });

  test('it displays level 1 in initial state', async function (assert) {
    await render(<template><XpBadge /></template>);
    assert.dom('.xp-badge__level-text').hasText('1');
  });

  test('it displays 0 XP in initial state', async function (assert) {
    await render(<template><XpBadge /></template>);
    assert.dom('.xp-badge__xp-text').hasText('0 XP');
  });

  test('it displays updated XP after addRightAnswerXp', async function (assert) {
    const gamification = this.owner.lookup('service:gamification');
    gamification.addRightAnswerXp();
    await render(<template><XpBadge /></template>);
    assert.dom('.xp-badge__xp-text').hasText('10 XP');
  });

  test('it displays correct level for higher XP', async function (assert) {
    // Level 2 requires 100 XP
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 150,
        currentStreak: 0,
        longestStreak: 0,
        lastActiveDate: null,
        exercisesCompleted: 0,
        perfectExercises: 0,
        badges: {},
      }),
    );

    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );

    await render(<template><XpBadge /></template>);
    assert.dom('.xp-badge__level-text').hasText('2');
    assert.dom('.xp-badge__xp-text').hasText('150 XP');
  });
});
