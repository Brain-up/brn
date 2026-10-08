import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Schema | user-daily-time-table-statistics', function (hooks) {
  setupTest(hooks);

  test('creates a user-daily-time-table-statistics record with all attributes', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('user-daily-time-table-statistics', {
      seriesName: 'Words Series',
      allDoneExercises: 10,
      uniqueDoneExercises: 8,
      repeatedExercises: 2,
      doneExercisesSuccessfullyFromFirstTime: 6,
      listenWordsCount: 50,
    });
    assert.ok(record, 'record is created');
    assert.strictEqual(record.seriesName, 'Words Series');
    assert.strictEqual(record.allDoneExercises, 10);
    assert.strictEqual(record.uniqueDoneExercises, 8);
    assert.strictEqual(record.repeatedExercises, 2);
    assert.strictEqual(record.doneExercisesSuccessfullyFromFirstTime, 6);
    assert.strictEqual(record.listenWordsCount, 50);
  });

  test('creates a record with default undefined attributes', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('user-daily-time-table-statistics', {});
    assert.ok(record, 'record is created');
    assert.strictEqual(record.seriesName, undefined);
    assert.strictEqual(record.allDoneExercises, undefined);
    assert.strictEqual(record.uniqueDoneExercises, undefined);
    assert.strictEqual(record.repeatedExercises, undefined);
    assert.strictEqual(record.doneExercisesSuccessfullyFromFirstTime, undefined);
    assert.strictEqual(record.listenWordsCount, undefined);
  });

  test('creates a record with zero values', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('user-daily-time-table-statistics', {
      allDoneExercises: 0,
      uniqueDoneExercises: 0,
      repeatedExercises: 0,
      doneExercisesSuccessfullyFromFirstTime: 0,
      listenWordsCount: 0,
    });
    assert.strictEqual(record.allDoneExercises, 0);
    assert.strictEqual(record.uniqueDoneExercises, 0);
    assert.strictEqual(record.repeatedExercises, 0);
    assert.strictEqual(record.doneExercisesSuccessfullyFromFirstTime, 0);
    assert.strictEqual(record.listenWordsCount, 0);
  });
});
