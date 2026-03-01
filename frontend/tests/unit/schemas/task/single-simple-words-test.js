import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { ExerciseMechanism } from 'brn/utils/exercise-types';

module('Unit | Schema | task/single-simple-words', function (hooks) {
  setupTest(hooks);

  test('creates a task/single-simple-words record', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/single-simple-words', {
      name: 'Words Task',
      order: 2,
      exerciseType: 'SINGLE_SIMPLE_WORDS',
    });
    assert.ok(record, 'record is created');
    assert.strictEqual(record.name, 'Words Task');
    assert.strictEqual(record.order, 2);
    assert.strictEqual(record.exerciseType, 'SINGLE_SIMPLE_WORDS');
  });

  test('exerciseMechanism extension getter returns WORDS', function (assert) {
    // The extension getter is defined on the extension object directly.
    // When the attribute is not set by the API, the extension provides the default.
    const { TaskSingleSimpleWordsExtension } = require('brn/schemas/task/single-simple-words');
    const getter = Object.getOwnPropertyDescriptor(
      TaskSingleSimpleWordsExtension.features,
      'exerciseMechanism',
    );
    assert.ok(getter, 'exerciseMechanism getter exists');
    assert.strictEqual(getter.get.call({}), ExerciseMechanism.WORDS);
  });

  test('has base task fields', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/single-simple-words', {
      name: 'Test',
      active: true,
      repetitionCount: 5,
      shouldBeWithPictures: true,
    });
    assert.strictEqual(record.name, 'Test');
    assert.strictEqual(record.active, true);
    assert.strictEqual(record.repetitionCount, 5);
    assert.strictEqual(record.shouldBeWithPictures, true);
  });

  test('has local fields with defaults', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/single-simple-words', {});
    assert.strictEqual(record.isManuallyCompleted, false);
    assert.strictEqual(record.nextAttempt, false);
    assert.strictEqual(record.available, false);
  });
});
