import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { isEmpty } from '@ember/utils';
import { isArray } from '@ember/array';
import Service from '@ember/service';

module('Unit | Service | tasksManager', function (hooks) {
  setupTest(hooks);

  test('has empty completedTasks array after init', function (assert) {
    let service = this.owner.lookup('service:tasks-manager');
    assert.ok(
      isEmpty(service.completedTasks) && isArray(service.completedTasks),
      'is empty array',
    );
  });

  test('save as completed', function (assert) {
    let service = this.owner.lookup('service:tasks-manager');
    const targetObj = {};
    service.saveAsCompleted(targetObj);
    assert.ok(
      service.completedTasks.includes(targetObj),
      'adds target object to completedTasks',
    );
  });

  test('isCompleted prop', function (assert) {
    let service = this.owner.lookup('service:tasks-manager');
    const targetObj = {};
    service.saveAsCompleted(targetObj);
    assert.ok(
      service.isCompleted(targetObj),
      'returns true if parameter is present in completedTasks',
    );
  });

  test('loadTodayCompletedExercises populates completedExerciseIds', async function (assert) {
    class MockNetwork extends Service {
      getStudyHistoriesV2(from, to) {
        assert.ok(from.match(/^\d{4}-\d{2}-\d{2}T00:00:00$/), 'from is start of day');
        assert.ok(to.match(/^\d{4}-\d{2}-\d{2}T23:59:59$/), 'to is end of day');
        return Promise.resolve([
          { exerciseId: 42 },
          { exerciseId: 99 },
        ]);
      }
    }
    this.owner.register('service:network', MockNetwork);
    const service = this.owner.lookup('service:tasks-manager');

    await service.loadTodayCompletedExercises();

    assert.true(service.completedExerciseIds.has('42'), 'has exercise 42');
    assert.true(service.completedExerciseIds.has('99'), 'has exercise 99');
    assert.strictEqual(service.completedExerciseIds.size, 2, 'has exactly 2 entries');
  });

  test('loadTodayCompletedExercises skips null exerciseIds but keeps zero', async function (assert) {
    class MockNetwork extends Service {
      getStudyHistoriesV2() {
        return Promise.resolve([
          { exerciseId: 0 },
          { exerciseId: null },
          { exerciseId: undefined },
          { exerciseId: 5 },
        ]);
      }
    }
    this.owner.register('service:network', MockNetwork);
    const service = this.owner.lookup('service:tasks-manager');

    await service.loadTodayCompletedExercises();

    assert.true(service.completedExerciseIds.has('0'), 'keeps exerciseId 0');
    assert.true(service.completedExerciseIds.has('5'), 'keeps exerciseId 5');
    assert.strictEqual(service.completedExerciseIds.size, 2, 'skips null and undefined');
  });

  test('loadTodayCompletedExercises handles API failure gracefully', async function (assert) {
    class MockNetwork extends Service {
      getStudyHistoriesV2() {
        return Promise.reject(new Error('network error'));
      }
    }
    this.owner.register('service:network', MockNetwork);
    const service = this.owner.lookup('service:tasks-manager');

    await service.loadTodayCompletedExercises();

    assert.strictEqual(service.completedExerciseIds.size, 0, 'set stays empty on failure');
  });
});
