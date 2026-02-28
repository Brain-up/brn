import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';

module('Unit | Route | group/series/subgroup/exercise', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let route = this.owner.lookup('route:group/series/subgroup/exercise');
    assert.ok(route);
  });

  test('afterModel resets isAvailable then sets based on network response', async function (assert) {
    class MockNetwork extends Service {
      availableExercises() {
        return Promise.resolve(['50']);
      }
    }

    this.owner.register('service:network', MockNetwork);
    const route = this.owner.lookup('route:group/series/subgroup/exercise');
    route.isAvailable = true;

    await route.afterModel({ id: '50' });

    assert.true(route.isAvailable, 'isAvailable set to true when exercise is available');
  });

  test('afterModel sets isAvailable false when exercise not in available list', async function (assert) {
    class MockNetwork extends Service {
      availableExercises() {
        return Promise.resolve(['99']);
      }
    }

    this.owner.register('service:network', MockNetwork);
    const route = this.owner.lookup('route:group/series/subgroup/exercise');
    route.isAvailable = true;

    await route.afterModel({ id: '50' });

    assert.false(route.isAvailable, 'isAvailable set to false when exercise not available');
  });

  test('afterModel passes exercise id to availableExercises', async function (assert) {
    let passedIds = [];

    class MockNetwork extends Service {
      availableExercises(ids) {
        passedIds = ids;
        return Promise.resolve([]);
      }
    }

    this.owner.register('service:network', MockNetwork);
    const route = this.owner.lookup('route:group/series/subgroup/exercise');

    await route.afterModel({ id: '77' });

    assert.deepEqual(passedIds, ['77'], 'exercise id passed to network');
  });

  test('redirect does not check isAvailable during Ember.testing', function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise');
    route.isAvailable = false;  // not available, but Ember.testing is true

    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };

    // Even with isAvailable=false, redirect proceeds because Ember.testing is true
    route.redirect(
      {
        id: '50',
        tasks: [{ id: '100', order: 1 }],
        sortedTasks: [{ id: '100', order: 1 }],
      },
      {
        to: {
          name: 'group.series.subgroup.exercise.index',
          paramNames: [],
        },
      },
    );

    // Should NOT redirect to subgroup (the !isAvailable guard is skipped in testing)
    assert.strictEqual(
      transitionArgs[0],
      'group.series.subgroup.exercise.task',
      'redirects to task, not to subgroup',
    );
  });

  test('redirect warns and redirects when no tasks', function (assert) {
    let transitionArgs = [];
    let consoleWarned = false;
    const originalWarn = console.warn;
    console.warn = () => {
      consoleWarned = true;
    };

    try {
      const route = this.owner.lookup('route:group/series/subgroup/exercise');
      route.isAvailable = true;

      // Stub the router directly
      route.router = {
        transitionTo(...args) {
          transitionArgs = args;
        },
      };

      // Mock paramsFor
      route.paramsFor = (routeName) => {
        if (routeName === 'group.series') return { series_id: '10' };
        return {};
      };

      route.redirect(
        { id: '50', tasks: [], sortedTasks: [] },
        { to: { name: 'group.series.subgroup.exercise.index', paramNames: [] } },
      );

      assert.true(consoleWarned, 'console.warn was called');
      assert.strictEqual(transitionArgs[0], 'group.series');
      assert.strictEqual(transitionArgs[1], '10');
    } finally {
      console.warn = originalWarn;
    }
  });

  test('redirect goes to first task when on exercise.index', function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise');
    route.isAvailable = true;

    // Stub the router directly
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };

    route.redirect(
      {
        id: '50',
        tasks: [{ id: '100', order: 1 }],
        sortedTasks: [{ id: '100', order: 1 }],
      },
      {
        to: {
          name: 'group.series.subgroup.exercise.index',
          paramNames: [],
        },
      },
    );

    assert.strictEqual(
      transitionArgs[0],
      'group.series.subgroup.exercise.task',
    );
    assert.strictEqual(transitionArgs[1], '100');
  });

  test('resetController cleans up state', function (assert) {
    const route = this.owner.lookup('route:group/series/subgroup/exercise');
    const mockController = {
      showExerciseStats: true,
      exerciseStats: { some: 'data' },
      correctnessWidgetIsShown: true,
    };
    route.resetController(mockController, true);
    assert.false(mockController.showExerciseStats);
    assert.deepEqual(mockController.exerciseStats, {});
    assert.false(mockController.correctnessWidgetIsShown);
  });

  test('resetController does not clean up when not exiting', function (assert) {
    const route = this.owner.lookup('route:group/series/subgroup/exercise');
    const mockController = {
      showExerciseStats: true,
      exerciseStats: { some: 'data' },
      correctnessWidgetIsShown: true,
    };
    route.resetController(mockController, false);
    assert.true(mockController.showExerciseStats, 'not cleaned up');
  });

  test('deactivate clears state', function (assert) {
    const route = this.owner.lookup('route:group/series/subgroup/exercise');
    route.isAvailable = true;
    // Mock tasksManager to avoid depending on real service internals
    route.tasksManager = { clearCurrentCycleTaks() {} };
    route.deactivate();
    assert.false(route.isAvailable, 'isAvailable reset');
  });

  test('deactivate calls clearCurrentCycleTaks on tasksManager', function (assert) {
    const route = this.owner.lookup('route:group/series/subgroup/exercise');
    let cleared = false;
    route.tasksManager = { clearCurrentCycleTaks() { cleared = true; } };
    route.deactivate();
    assert.true(cleared, 'clearCurrentCycleTaks was called');
  });
});
