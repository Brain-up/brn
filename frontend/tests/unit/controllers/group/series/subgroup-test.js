import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';
import { A } from '@ember/array';
import { tracked } from '@glimmer/tracking';

module('Unit | Controller | group/series/subgroup', function (hooks) {
  setupTest(hooks);

  test('availableExercises is initialized as empty array', function (assert) {
    const controller = this.owner.lookup('controller:group/series/subgroup');
    assert.deepEqual(controller.availableExercises, []);
  });

  test('model setter triggers exerciseAvailabilityCalculationTask', async function (assert) {
    assert.expect(2);
    class MockNetwork extends Service {
      availableExercises(ids) {
        assert.deepEqual(ids, ['10', '20'], 'passes exercise ids to network');
        return Promise.resolve(['10']);
      }
    }
    this.owner.register('service:network', MockNetwork);
    const controller = this.owner.lookup('controller:group/series/subgroup');

    controller.model = [{ id: '10' }, { id: '20' }];
    // Wait for the keepLatest task to complete
    await controller.exerciseAvailabilityCalculationTask.last;

    assert.deepEqual(
      controller.availableExercises,
      ['10'],
      'availableExercises populated from network response',
    );
  });

  test('model setter with empty model does not call network', async function (assert) {
    class MockNetwork extends Service {
      availableExercises() {
        assert.ok(false, 'should not be called for null model');
        return Promise.resolve([]);
      }
    }
    this.owner.register('service:network', MockNetwork);
    const controller = this.owner.lookup('controller:group/series/subgroup');

    controller.model = null;
    await controller.exerciseAvailabilityCalculationTask.last;

    assert.deepEqual(controller.availableExercises, [], 'stays empty');
  });

  test('sets isManuallyCompleted on exercises matching completedExerciseIds', async function (assert) {
    const exercise1 = { id: '10', isManuallyCompleted: false };
    const exercise2 = { id: '20', isManuallyCompleted: false };

    class MockNetwork extends Service {
      availableExercises() {
        return Promise.resolve(['10', '20']);
      }
    }
    class MockTasksManager extends Service {
      @tracked completedTasks = A();
      @tracked completedCycleTasks = A();
      @tracked completedExerciseIds = new Set(['10']);
    }

    this.owner.register('service:network', MockNetwork);
    this.owner.register('service:tasks-manager', MockTasksManager);
    const controller = this.owner.lookup('controller:group/series/subgroup');

    controller.model = [exercise1, exercise2];
    await controller.exerciseAvailabilityCalculationTask.last;

    assert.true(
      exercise1.isManuallyCompleted,
      'matched exercise is marked as manually completed',
    );
    assert.false(
      exercise2.isManuallyCompleted,
      'non-matched exercise stays not completed',
    );
  });

  test('handles exercises with no matching completedExerciseIds gracefully', async function (assert) {
    const exercise1 = { id: '10', isManuallyCompleted: false };

    class MockNetwork extends Service {
      availableExercises() {
        return Promise.resolve(['10']);
      }
    }
    class MockTasksManager extends Service {
      @tracked completedTasks = A();
      @tracked completedCycleTasks = A();
      @tracked completedExerciseIds = new Set();
    }

    this.owner.register('service:network', MockNetwork);
    this.owner.register('service:tasks-manager', MockTasksManager);
    const controller = this.owner.lookup('controller:group/series/subgroup');

    controller.model = [exercise1];
    await controller.exerciseAvailabilityCalculationTask.last;

    assert.false(
      exercise1.isManuallyCompleted,
      'exercise stays not completed when completedExerciseIds is empty',
    );
  });
});
