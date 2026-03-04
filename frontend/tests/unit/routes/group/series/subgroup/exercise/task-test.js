import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Route | group/series/subgroup/exercise/task', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let route = this.owner.lookup('route:group/series/subgroup/exercise/task');
    assert.ok(route);
  });

  test('model finds task from exercise tasks by id', function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };

    // Mock modelFor to return an exercise with tasks
    route.modelFor = () => ({
      tasks: [
        { id: '100', name: 'Task 1' },
        { id: '200', name: 'Task 2' },
      ],
    });

    const result = route.model({ task_id: '200' });
    assert.strictEqual(result.id, '200');
    assert.strictEqual(result.name, 'Task 2');
    assert.strictEqual(transitionArgs.length, 0, 'no redirect');
  });

  test('model redirects when task not found', function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };

    route.modelFor = () => ({
      tasks: [{ id: '100', name: 'Task 1' }],
    });

    route.paramsFor = () => ({ exercise_id: '50' });

    const result = route.model({ task_id: '999' });
    assert.strictEqual(result, undefined, 'returns undefined');
    assert.strictEqual(transitionArgs[0], 'group.series.subgroup.exercise');
    assert.strictEqual(transitionArgs[1], '50');
  });

  test('model handles empty tasks array', function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };

    route.modelFor = () => ({ tasks: [] });
    route.paramsFor = () => ({ exercise_id: '50' });

    const result = route.model({ task_id: '100' });
    assert.strictEqual(result, undefined);
    assert.strictEqual(transitionArgs[0], 'group.series.subgroup.exercise');
  });

  test('model handles null tasks', function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };

    route.modelFor = () => ({ tasks: null });
    route.paramsFor = () => ({ exercise_id: '50' });

    const result = route.model({ task_id: '100' });
    assert.strictEqual(result, undefined);
    assert.strictEqual(transitionArgs[0], 'group.series.subgroup.exercise');
  });

  test('afterModel resets repetitionCount when task can interact', async function (assert) {
    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');

    const mockTask = {
      canInteract: true,
      repetitionCount: 5,
      exercise: { id: '50' },
    };

    await route.afterModel(mockTask, {
      to: { parent: { params: { exercise_id: '50' } } },
    });

    assert.strictEqual(mockTask.repetitionCount, 0, 'repetitionCount reset to 0');
  });

  test('afterModel does nothing when task is undefined', async function (assert) {
    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');

    // Should not throw
    await route.afterModel(undefined, {
      to: { parent: { params: { exercise_id: '50' } } },
    });

    assert.ok(true, 'no error thrown');
  });

  test('afterModel redirects to first task when canInteract is false', async function (assert) {
    let transitionArgs = [];
    let findRecordArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };
    route.store = {
      findRecord(type, id) {
        findRecordArgs = [type, id];
        return Promise.resolve({
          id: '50',
          sortedTasks: [{ id: '301', order: 1 }, { id: '302', order: 2 }],
        });
      },
    };
    route.paramsFor = (routeName) => {
      if (routeName === 'group.series') return { series_id: '7' };
      if (routeName === 'group.series.subgroup') return { subgroup_id: '8' };
      return {};
    };

    const mockTask = {
      canInteract: false,
      repetitionCount: 3,
      exercise: { id: '50' },
    };

    await route.afterModel(mockTask, {
      to: { parent: { params: { exercise_id: '50' } } },
    });

    assert.deepEqual(findRecordArgs, ['exercise', '50'], 'fetches exercise from store');
    assert.strictEqual(transitionArgs[0], 'group.series.subgroup.exercise.task');
    assert.strictEqual(transitionArgs[1], '7', 'series_id from paramsFor');
    assert.strictEqual(transitionArgs[2], '8', 'subgroup_id from paramsFor');
    assert.strictEqual(transitionArgs[3], '50', 'exercise id');
    assert.strictEqual(transitionArgs[4], '301', 'first sorted task id');
    assert.notStrictEqual(mockTask.repetitionCount, 0, 'repetitionCount NOT reset');
  });

  test('afterModel redirects when exercise id mismatches', async function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };
    route.store = {
      findRecord() {
        return Promise.resolve({
          id: '60',
          sortedTasks: [{ id: '400', order: 1 }],
        });
      },
    };
    route.paramsFor = (routeName) => {
      if (routeName === 'group.series') return { series_id: '7' };
      if (routeName === 'group.series.subgroup') return { subgroup_id: '8' };
      return {};
    };

    const mockTask = {
      canInteract: true,
      exercise: { id: '50' },  // task's exercise is 50
    };

    await route.afterModel(mockTask, {
      to: { parent: { params: { exercise_id: '60' } } },  // but URL says 60
    });

    assert.strictEqual(transitionArgs[0], 'group.series.subgroup.exercise.task');
    assert.strictEqual(transitionArgs[3], '60', 'redirects to correct exercise');
    assert.strictEqual(transitionArgs[4], '400', 'redirects to first task');
  });

  test('afterModel does not redirect when canInteract false but exercise has no sorted tasks', async function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };
    route.store = {
      findRecord() {
        return Promise.resolve({
          id: '50',
          sortedTasks: [],  // no tasks
        });
      },
    };
    route.paramsFor = () => ({});

    const mockTask = {
      canInteract: false,
      exercise: { id: '50' },
    };

    await route.afterModel(mockTask, {
      to: { parent: { params: { exercise_id: '50' } } },
    });

    assert.strictEqual(transitionArgs.length, 0, 'no redirect when no sorted tasks');
  });

  test('afterModel does not redirect when canInteract false and sortedTasks is null', async function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group/series/subgroup/exercise/task');
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };
    route.store = {
      findRecord() {
        return Promise.resolve({
          id: '50',
          sortedTasks: null,
        });
      },
    };
    route.paramsFor = () => ({});

    const mockTask = {
      canInteract: false,
      exercise: { id: '50' },
    };

    await route.afterModel(mockTask, {
      to: { parent: { params: { exercise_id: '50' } } },
    });

    assert.strictEqual(transitionArgs.length, 0, 'no redirect when sortedTasks is null');
  });
});
