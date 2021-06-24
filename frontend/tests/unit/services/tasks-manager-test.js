import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { isEmpty } from '@ember/utils';
import { isArray } from '@ember/array';

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
});
