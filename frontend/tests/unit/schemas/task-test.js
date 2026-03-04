import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Schema | task', function (hooks) {
  setupTest(hooks);

  test('creates a task record', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task', {
      name: 'Test Task',
      order: 1,
      active: true,
    });
    assert.ok(model);
    assert.strictEqual(model.name, 'Test Task');
    assert.strictEqual(model.order, 1);
  });

  test('sortChildrenBy returns order', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task', {});
    assert.strictEqual(model.sortChildrenBy, 'order');
  });

  test('children is always empty array (tasks are leaf nodes)', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task', {});
    assert.deepEqual(model.children, []);
  });

  test('sortedChildren is null (tasks are leaf nodes)', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task', {});
    assert.strictEqual(model.sortedChildren, null);
  });

  test('isCompleted is false with no tasks manager data', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task', {});
    assert.false(model.isCompleted);
  });

  test('isCompleted is true when saved as completed', function (assert) {
    const store = this.owner.lookup('service:store');
    const tasksManager = this.owner.lookup('service:tasks-manager');
    const model = store.createRecord('task', {});
    tasksManager.saveAsCompleted(model);
    assert.true(model.isCompleted);
  });

  test('completedInCurrentCycle defaults to false', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task', {});
    assert.false(model.completedInCurrentCycle);
  });

  test('completedInCurrentCycle can be set', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task', {});
    model.completedInCurrentCycle = true;
    assert.true(model.completedInCurrentCycle);
  });

  test('savePassed marks task as completed', function (assert) {
    const store = this.owner.lookup('service:store');
    const tasksManager = this.owner.lookup('service:tasks-manager');
    const model = store.createRecord('task', {});
    model.savePassed();
    assert.true(tasksManager.isCompleted(model));
  });

  test('isLastTask is true when task is the only task in exercise', function (assert) {
    const store = this.owner.lookup('service:store');
    // Create an exercise, then a task pointing to it
    const exercise = store.createRecord('exercise', { name: 'Ex1' });
    const model = store.createRecord('task', { exercise, order: 1 });
    // The only task in exercise → nextTask is null → isLastTask is true
    assert.true(model.isLastTask);
  });

  test('local fields have correct defaults', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task', {});
    assert.false(model.isManuallyCompleted, 'isManuallyCompleted defaults false');
    assert.false(model.nextAttempt, 'nextAttempt defaults false');
    assert.false(model.available, 'available defaults false');
  });
});
