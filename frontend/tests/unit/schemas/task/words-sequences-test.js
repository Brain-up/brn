import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { ExerciseMechanism } from 'brn/utils/exercise-types';

module('Unit | Schema | task/words-sequences', function (hooks) {
  setupTest(hooks);

  test('creates a task/words-sequences record', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/words-sequences', {
      name: 'Sentence Task',
      template: 'I <like cats>',
      order: 1,
    });
    assert.ok(record, 'record is created');
    assert.strictEqual(record.name, 'Sentence Task');
    assert.strictEqual(record.template, 'I <like cats>');
    assert.strictEqual(record.order, 1);
  });

  test('exerciseMechanism attribute stores the value from createRecord', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/words-sequences', {
      exerciseMechanism: 'MATRIX',
    });
    assert.strictEqual(record.exerciseMechanism, ExerciseMechanism.MATRIX);
  });

  test('selectedItemsOrder parses template correctly', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/words-sequences', {
      template: 'The <big red> dog',
    });
    assert.deepEqual(record.selectedItemsOrder, ['big', 'red']);
  });

  test('selectedItemsOrder parses single word template', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/words-sequences', {
      template: '<word>',
    });
    assert.deepEqual(record.selectedItemsOrder, ['word']);
  });

  test('possibleTasks computes cartesian product from answerOptions', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/words-sequences', {
      template: '<a b>',
      answerOptions: {
        a: ['x', 'y'],
        b: ['1', '2'],
      },
    });
    const tasks = record.possibleTasks;
    // Cartesian product of [x,y] x [1,2] = 4 combinations
    assert.strictEqual(tasks.length, 4);
    // Each task should be an array of 2 elements
    tasks.forEach((task) => {
      assert.strictEqual(task.length, 2);
    });
  });

  test('possibleTasks filters to only keys present in answerOptions', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('task/words-sequences', {
      template: '<a b c>',
      answerOptions: {
        a: ['x'],
        b: ['y'],
        // 'c' is not in answerOptions, so it is filtered out
      },
    });
    const tasks = record.possibleTasks;
    // Only a and b are in answerOptions: [x] x [y] = 1 combination
    assert.strictEqual(tasks.length, 1);
    assert.deepEqual(tasks[0], ['x', 'y']);
  });

  test('selectedItemsOrder falls back to exercise.template when task template is empty', function (assert) {
    const store = this.owner.lookup('service:store');
    const exercise = store.createRecord('exercise', {
      template: '<OBJECT OBJECT_ACTION>',
    });
    const record = store.createRecord('task/words-sequences', {
      exercise,
      // no template on the task itself
    });
    assert.deepEqual(
      record.selectedItemsOrder,
      ['OBJECT', 'OBJECT_ACTION'],
      'reads template from exercise when task template is empty',
    );
  });

  test('selectedItemsOrder returns empty array when no template anywhere', function (assert) {
    const store = this.owner.lookup('service:store');
    const exercise = store.createRecord('exercise', {});
    const record = store.createRecord('task/words-sequences', {
      exercise,
    });
    assert.deepEqual(
      record.selectedItemsOrder,
      [],
      'returns empty array when no template found',
    );
  });
});
