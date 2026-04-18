import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';

module('Unit | Controller | group/series/subgroup/exercise', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let controller = this.owner.lookup(
      'controller:group/series/subgroup/exercise',
    );
    assert.ok(controller);
  });

  module('enableNextExercise', function (innerHooks) {
    innerHooks.beforeEach(function () {
      class MockTasksManager extends Service {
        @tracked completedExerciseIds = new Set();
      }
      this.owner.register('service:tasks-manager', MockTasksManager);
    });

    function makeModel(id, siblings = []) {
      const model = { id, isManuallyCompleted: false };
      const exercises = [model, ...siblings];
      model.parent = { exercises };
      return model;
    }

    test('marks current exercise as manually completed', function (assert) {
      const controller = this.owner.lookup(
        'controller:group/series/subgroup/exercise',
      );
      const model = makeModel('10');
      controller.model = model;

      controller.enableNextExercise(model);

      assert.true(
        model.isManuallyCompleted,
        'current model is marked manually completed',
      );
    });

    test('enables the next sibling exercise', function (assert) {
      const controller = this.owner.lookup(
        'controller:group/series/subgroup/exercise',
      );
      const next = { id: '11', available: false };
      const model = makeModel('10', [next]);
      controller.model = model;

      controller.enableNextExercise(model);

      assert.true(next.available, 'next sibling is marked available');
    });

    test('adds completed exercise id to tasksManager.completedExerciseIds', function (assert) {
      const controller = this.owner.lookup(
        'controller:group/series/subgroup/exercise',
      );
      const tasksManager = this.owner.lookup('service:tasks-manager');
      const model = makeModel('42');
      controller.model = model;

      assert.false(
        tasksManager.completedExerciseIds.has('42'),
        'not present before call',
      );

      controller.enableNextExercise(model);

      assert.true(
        tasksManager.completedExerciseIds.has('42'),
        'id is added after enableNextExercise',
      );
    });

    test('assigns a fresh Set so @tracked reactivity fires', function (assert) {
      const controller = this.owner.lookup(
        'controller:group/series/subgroup/exercise',
      );
      const tasksManager = this.owner.lookup('service:tasks-manager');
      const before = tasksManager.completedExerciseIds;
      const model = makeModel('7');
      controller.model = model;

      controller.enableNextExercise(model);

      assert.notStrictEqual(
        tasksManager.completedExerciseIds,
        before,
        'completedExerciseIds is a new Set instance',
      );
    });

    test('preserves previously completed exercise ids', function (assert) {
      class MockTasksManagerWithHistory extends Service {
        @tracked completedExerciseIds = new Set(['1', '2']);
      }
      this.owner.register('service:tasks-manager', MockTasksManagerWithHistory);
      const controller = this.owner.lookup(
        'controller:group/series/subgroup/exercise',
      );
      const tasksManager = this.owner.lookup('service:tasks-manager');
      const model = makeModel('3');
      controller.model = model;

      controller.enableNextExercise(model);

      assert.deepEqual(
        [...tasksManager.completedExerciseIds].sort(),
        ['1', '2', '3'],
        'earlier ids are kept alongside the new one',
      );
    });

    test('coerces numeric ids to strings for consistency with server lookup', function (assert) {
      const controller = this.owner.lookup(
        'controller:group/series/subgroup/exercise',
      );
      const tasksManager = this.owner.lookup('service:tasks-manager');
      const model = makeModel(42);
      controller.model = model;

      controller.enableNextExercise(model);

      assert.true(
        tasksManager.completedExerciseIds.has('42'),
        'stored as string even when id was a number',
      );
    });

    test('skips tasksManager update when model.id is null', function (assert) {
      const controller = this.owner.lookup(
        'controller:group/series/subgroup/exercise',
      );
      const tasksManager = this.owner.lookup('service:tasks-manager');
      const before = tasksManager.completedExerciseIds;
      const model = makeModel(null);
      controller.model = model;

      controller.enableNextExercise(model);

      assert.strictEqual(
        tasksManager.completedExerciseIds,
        before,
        'completedExerciseIds untouched for null id',
      );
    });
  });
});
