import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Service | gamification', function (hooks) {
  setupTest(hooks);

  hooks.beforeEach(function () {
    localStorage.removeItem('brn-gamification');
  });

  hooks.afterEach(function () {
    localStorage.removeItem('brn-gamification');
  });

  // ---------------------------------------------------------------------------
  // Initial state
  // ---------------------------------------------------------------------------

  test('initial state has 0 XP and level 1', function (assert) {
    const service = this.owner.lookup('service:gamification');
    assert.strictEqual(service.totalXp, 0);
    assert.strictEqual(service.level, 1);
  });

  test('initial state has no badges', function (assert) {
    const service = this.owner.lookup('service:gamification');
    assert.deepEqual(service.badges, {});
  });

  test('initial state has 0 streak', function (assert) {
    const service = this.owner.lookup('service:gamification');
    assert.strictEqual(service.currentStreak, 0);
    assert.strictEqual(service.longestStreak, 0);
  });

  test('initial state has 0 exercises completed', function (assert) {
    const service = this.owner.lookup('service:gamification');
    assert.strictEqual(service.exercisesCompleted, 0);
    assert.strictEqual(service.perfectExercises, 0);
  });

  test('initial state has 0 sessionXp', function (assert) {
    const service = this.owner.lookup('service:gamification');
    assert.strictEqual(service.sessionXp, 0);
  });

  // ---------------------------------------------------------------------------
  // addRightAnswerXp
  // ---------------------------------------------------------------------------

  test('addRightAnswerXp adds 10 XP', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.addRightAnswerXp();
    assert.strictEqual(service.totalXp, 10);
    assert.strictEqual(service.sessionXp, 10);
  });

  test('addRightAnswerXp accumulates XP across multiple calls', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.addRightAnswerXp();
    service.addRightAnswerXp();
    service.addRightAnswerXp();
    assert.strictEqual(service.totalXp, 30);
    assert.strictEqual(service.sessionXp, 30);
  });

  test('addRightAnswerXp sets lastXpGain to 10', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.addRightAnswerXp();
    assert.strictEqual(service.lastXpGain, 10);
  });

  test('addRightAnswerXp sets showXpPopup to true', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.addRightAnswerXp();
    assert.true(service.showXpPopup);
  });

  // ---------------------------------------------------------------------------
  // completeExercise - XP calculation
  // ---------------------------------------------------------------------------

  test('completeExercise with perfect score gives 40 XP (25 completion + 15 perfect + 0 streak bonus)', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(service.totalXp, 40);
  });

  test('completeExercise with wrong answers gives 25 XP (25 completion + 0 perfect + 0 streak bonus)', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 3, countedSeconds: 90 });
    assert.strictEqual(service.totalXp, 25);
  });

  test('completeExercise includes streak bonus when streak is active', function (assert) {
    // Set up state with an existing streak by pre-seeding localStorage
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayStr = yesterday.toISOString().split('T')[0];

    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 0,
        currentStreak: 3,
        longestStreak: 3,
        lastActiveDate: yesterdayStr,
        exercisesCompleted: 0,
        perfectExercises: 0,
        badges: {},
      }),
    );

    // Re-create service to pick up localStorage state
    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );
    const freshService = this.owner.lookup('service:gamification');

    // streak was 3, completing exercise updates streak to 4 (consecutive day),
    // but streak bonus is calculated from currentStreak BEFORE updateStreak mutates it.
    // Actually, looking at the code: streakBonus uses this._state.currentStreak (3),
    // then updateStreak changes newState.currentStreak to 4.
    // So streak bonus = min(3 * 5, 50) = 15
    // Total with no wrong answers: 25 + 15 + 15 = 55
    freshService.completeExercise({ wrongAnswersCount: 0, countedSeconds: 120 });
    assert.strictEqual(freshService.totalXp, 55);
  });

  test('streak bonus is capped at 50', function (assert) {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayStr = yesterday.toISOString().split('T')[0];

    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 0,
        currentStreak: 20,
        longestStreak: 20,
        lastActiveDate: yesterdayStr,
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
    const freshService = this.owner.lookup('service:gamification');

    // streak bonus = min(20 * 5, 50) = 50 (capped)
    // Total with wrong answers: 25 + 0 + 50 = 75
    freshService.completeExercise({ wrongAnswersCount: 2, countedSeconds: 120 });
    assert.strictEqual(freshService.totalXp, 75);
  });

  test('completeExercise increments exercisesCompleted', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(service.exercisesCompleted, 1);
    service.completeExercise({ wrongAnswersCount: 1, countedSeconds: 30 });
    assert.strictEqual(service.exercisesCompleted, 2);
  });

  test('completeExercise increments perfectExercises only on perfect score', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(service.perfectExercises, 1);
    service.completeExercise({ wrongAnswersCount: 1, countedSeconds: 30 });
    assert.strictEqual(service.perfectExercises, 1);
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(service.perfectExercises, 2);
  });

  test('completeExercise updates sessionXp and lastXpGain', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(service.sessionXp, 40);
    assert.strictEqual(service.lastXpGain, 40);
    assert.true(service.showXpPopup);
  });

  // ---------------------------------------------------------------------------
  // Streak logic
  // ---------------------------------------------------------------------------

  test('first exercise sets streak to 1', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(service.currentStreak, 1);
  });

  test('completing exercise on same day does not change streak', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(service.currentStreak, 1);
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(service.currentStreak, 1);
  });

  test('consecutive day increments streak', function (assert) {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayStr = yesterday.toISOString().split('T')[0];

    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 40,
        currentStreak: 1,
        longestStreak: 1,
        lastActiveDate: yesterdayStr,
        exercisesCompleted: 1,
        perfectExercises: 1,
        badges: {},
      }),
    );

    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );
    const freshService = this.owner.lookup('service:gamification');

    freshService.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(freshService.currentStreak, 2);
  });

  test('gap in activity resets streak to 1', function (assert) {
    // Last active 3 days ago (not yesterday)
    const threeDaysAgo = new Date();
    threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);
    const threeDaysAgoStr = threeDaysAgo.toISOString().split('T')[0];

    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 100,
        currentStreak: 5,
        longestStreak: 5,
        lastActiveDate: threeDaysAgoStr,
        exercisesCompleted: 5,
        perfectExercises: 3,
        badges: {},
      }),
    );

    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );
    const freshService = this.owner.lookup('service:gamification');

    freshService.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(freshService.currentStreak, 1);
  });

  test('longestStreak is preserved when streak resets', function (assert) {
    const threeDaysAgo = new Date();
    threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);
    const threeDaysAgoStr = threeDaysAgo.toISOString().split('T')[0];

    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 100,
        currentStreak: 5,
        longestStreak: 10,
        lastActiveDate: threeDaysAgoStr,
        exercisesCompleted: 5,
        perfectExercises: 3,
        badges: {},
      }),
    );

    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );
    const freshService = this.owner.lookup('service:gamification');

    freshService.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(freshService.currentStreak, 1);
    assert.strictEqual(freshService.longestStreak, 10, 'longestStreak unchanged');
  });

  // ---------------------------------------------------------------------------
  // Badge awarding
  // ---------------------------------------------------------------------------

  test('first_exercise badge is awarded on first exercise completion', function (assert) {
    const service = this.owner.lookup('service:gamification');
    assert.strictEqual(service.badges['first_exercise'], undefined);
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.ok(service.badges['first_exercise'], 'first_exercise badge was awarded');
  });

  test('perfect_score badge is awarded on 0 wrong answers', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 90 });
    assert.ok(service.badges['perfect_score'], 'perfect_score badge was awarded');
  });

  test('perfect_score badge is NOT awarded when there are wrong answers', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 2, countedSeconds: 90 });
    assert.notOk(service.badges['perfect_score']);
  });

  test('speed_listener badge is awarded when exercise is completed in under 60 seconds', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 59 });
    assert.ok(service.badges['speed_listener'], 'speed_listener badge was awarded');
  });

  test('speed_listener badge is NOT awarded when exercise takes 60 seconds or more', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 60 });
    assert.notOk(service.badges['speed_listener']);
  });

  test('badge is only awarded once (not overwritten)', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    const firstTimestamp = service.badges['first_exercise'];
    assert.ok(firstTimestamp, 'badge was awarded');

    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    assert.strictEqual(
      service.badges['first_exercise'],
      firstTimestamp,
      'badge timestamp did not change on second completion',
    );
  });

  // ---------------------------------------------------------------------------
  // Level calculation
  // ---------------------------------------------------------------------------

  test('level is 1 at 0 XP', function (assert) {
    const service = this.owner.lookup('service:gamification');
    assert.strictEqual(service.level, 1);
  });

  test('level is 1 at 99 XP (just below level 2 threshold)', function (assert) {
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 99,
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
    const freshService = this.owner.lookup('service:gamification');
    assert.strictEqual(freshService.level, 1);
  });

  test('level is 2 at 100 XP (level 2 threshold = 2 * 50 = 100)', function (assert) {
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 100,
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
    const freshService = this.owner.lookup('service:gamification');
    assert.strictEqual(freshService.level, 2);
  });

  test('level is 3 at 250 XP (100 + 150 = 250)', function (assert) {
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 250,
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
    const freshService = this.owner.lookup('service:gamification');
    assert.strictEqual(freshService.level, 3);
  });

  test('level is 2 at 249 XP (just below level 3 threshold)', function (assert) {
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 249,
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
    const freshService = this.owner.lookup('service:gamification');
    assert.strictEqual(freshService.level, 2);
  });

  // ---------------------------------------------------------------------------
  // xpProgress percentage
  // ---------------------------------------------------------------------------

  test('xpProgress is 0 at start of a level', function (assert) {
    const service = this.owner.lookup('service:gamification');
    // At 0 XP, level 1, xpForCurrentLevel = 0, xpForNextLevel = 100
    // xpIntoLevel = 0, progress = 0%
    assert.strictEqual(service.xpProgress, 0);
  });

  test('xpProgress is 50 at midpoint of level', function (assert) {
    // Level 1: xpForCurrentLevel = 0, xpForNextLevel = 100
    // At 50 XP: xpIntoLevel = 50, range = 100, progress = 50%
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 50,
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
    const freshService = this.owner.lookup('service:gamification');
    assert.strictEqual(freshService.xpProgress, 50);
  });

  test('xpProgress resets at start of new level', function (assert) {
    // Level 2 starts at 100 XP, next level at 250
    // At 100 XP: xpIntoLevel = 0, range = 150, progress = 0%
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 100,
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
    const freshService = this.owner.lookup('service:gamification');
    assert.strictEqual(freshService.xpProgress, 0);
  });

  // ---------------------------------------------------------------------------
  // resetSession
  // ---------------------------------------------------------------------------

  test('resetSession resets sessionXp to 0', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.addRightAnswerXp();
    service.addRightAnswerXp();
    assert.strictEqual(service.sessionXp, 20);
    service.resetSession();
    assert.strictEqual(service.sessionXp, 0);
  });

  test('resetSession does not affect totalXp', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.addRightAnswerXp();
    service.addRightAnswerXp();
    assert.strictEqual(service.totalXp, 20);
    service.resetSession();
    assert.strictEqual(service.totalXp, 20);
  });

  // ---------------------------------------------------------------------------
  // localStorage persistence
  // ---------------------------------------------------------------------------

  test('state is persisted to localStorage after addRightAnswerXp', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.addRightAnswerXp();
    const stored = JSON.parse(localStorage.getItem('brn-gamification'));
    assert.strictEqual(stored.totalXp, 10);
  });

  test('state is persisted to localStorage after completeExercise', function (assert) {
    const service = this.owner.lookup('service:gamification');
    service.completeExercise({ wrongAnswersCount: 0, countedSeconds: 30 });
    const stored = JSON.parse(localStorage.getItem('brn-gamification'));
    assert.strictEqual(stored.totalXp, 40);
    assert.strictEqual(stored.exercisesCompleted, 1);
    assert.strictEqual(stored.perfectExercises, 1);
    assert.ok(stored.badges['first_exercise']);
  });

  test('state is loaded from localStorage on service initialization', function (assert) {
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 500,
        currentStreak: 7,
        longestStreak: 14,
        lastActiveDate: '2025-01-15',
        exercisesCompleted: 20,
        perfectExercises: 12,
        badges: { first_exercise: '2025-01-01T00:00:00.000Z' },
      }),
    );

    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );
    const freshService = this.owner.lookup('service:gamification');

    assert.strictEqual(freshService.totalXp, 500);
    assert.strictEqual(freshService.currentStreak, 7);
    assert.strictEqual(freshService.longestStreak, 14);
    assert.strictEqual(freshService.exercisesCompleted, 20);
    assert.strictEqual(freshService.perfectExercises, 12);
    assert.strictEqual(
      freshService.badges['first_exercise'],
      '2025-01-01T00:00:00.000Z',
    );
  });

  // ---------------------------------------------------------------------------
  // Corrupted localStorage
  // ---------------------------------------------------------------------------

  test('corrupted localStorage falls back to default state', function (assert) {
    localStorage.setItem('brn-gamification', 'not-valid-json{{{');

    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );
    const freshService = this.owner.lookup('service:gamification');

    assert.strictEqual(freshService.totalXp, 0);
    assert.strictEqual(freshService.level, 1);
    assert.strictEqual(freshService.currentStreak, 0);
    assert.deepEqual(freshService.badges, {});
  });

  test('partially corrupted localStorage uses defaults for missing fields', function (assert) {
    localStorage.setItem(
      'brn-gamification',
      JSON.stringify({
        totalXp: 200,
        // Missing other fields
      }),
    );

    this.owner.unregister('service:gamification');
    this.owner.register(
      'service:gamification',
      class extends (this.owner.factoryFor('service:gamification').class) {},
    );
    const freshService = this.owner.lookup('service:gamification');

    assert.strictEqual(freshService.totalXp, 200, 'totalXp loaded from storage');
    assert.strictEqual(freshService.currentStreak, 0, 'missing currentStreak defaults to 0');
    assert.strictEqual(freshService.longestStreak, 0, 'missing longestStreak defaults to 0');
    assert.strictEqual(freshService.lastActiveDate, null, 'missing lastActiveDate defaults to null');
    assert.strictEqual(freshService.exercisesCompleted, 0, 'missing exercisesCompleted defaults to 0');
    assert.strictEqual(freshService.perfectExercises, 0, 'missing perfectExercises defaults to 0');
    assert.deepEqual(freshService.badges, {}, 'missing badges defaults to empty object');
  });
});
