import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Schema | series', function (hooks) {
  setupTest(hooks);

  test('creates a series record', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('series', {
      name: 'Words',
      description: 'Word exercises',
      level: 1,
      kind: 'SINGLE_SIMPLE_WORDS',
      active: true,
      order: 1,
    });
    assert.ok(model);
    assert.strictEqual(model.name, 'Words');
    assert.strictEqual(model.kind, 'SINGLE_SIMPLE_WORDS');
  });

  test('sortedExercises sorts by order', function (assert) {
    const store = this.owner.lookup('service:store');
    const ex1 = store.createRecord('exercise', { order: 3, name: 'C' });
    const ex2 = store.createRecord('exercise', { order: 1, name: 'A' });
    const ex3 = store.createRecord('exercise', { order: 2, name: 'B' });
    const series = store.createRecord('series', {
      name: 'Test',
      exercises: [ex1, ex2, ex3],
    });
    const sorted = series.sortedExercises;
    assert.strictEqual(sorted[0].name, 'A');
    assert.strictEqual(sorted[1].name, 'B');
    assert.strictEqual(sorted[2].name, 'C');
  });

  test('sortedChildren delegates to sortedExercises', function (assert) {
    const store = this.owner.lookup('service:store');
    const ex1 = store.createRecord('exercise', { order: 2, name: 'B' });
    const ex2 = store.createRecord('exercise', { order: 1, name: 'A' });
    const series = store.createRecord('series', {
      name: 'Test',
      exercises: [ex1, ex2],
    });
    const children = series.sortedChildren;
    assert.strictEqual(children[0].name, 'A');
    assert.strictEqual(children[1].name, 'B');
  });

  test('groupedByNameExercises groups and sorts exercises', function (assert) {
    const store = this.owner.lookup('service:store');
    const ex1 = store.createRecord('exercise', {
      order: 2,
      name: 'Speech',
    });
    const ex2 = store.createRecord('exercise', {
      order: 1,
      name: 'Speech',
    });
    const ex3 = store.createRecord('exercise', {
      order: 1,
      name: 'Words',
    });
    const series = store.createRecord('series', {
      name: 'Test',
      exercises: [ex1, ex2, ex3],
    });
    const grouped = series.groupedByNameExercises;
    assert.ok(grouped['Speech'], 'has Speech group');
    assert.ok(grouped['Words'], 'has Words group');
    assert.strictEqual(grouped['Speech'].length, 2);
    assert.strictEqual(grouped['Speech'][0].order, 1, 'sorted within group');
    assert.strictEqual(grouped['Speech'][1].order, 2);
    assert.strictEqual(grouped['Words'].length, 1);
  });

  test('children returns exercises', function (assert) {
    const store = this.owner.lookup('service:store');
    const ex = store.createRecord('exercise', { name: 'Ex' });
    const series = store.createRecord('series', {
      exercises: [ex],
    });
    assert.strictEqual(series.children.length, 1);
  });

  test('children returns empty array when exercises is null', function (assert) {
    const store = this.owner.lookup('service:store');
    const series = store.createRecord('series', {});
    const children = series.children;
    assert.ok(Array.isArray(children));
    assert.strictEqual(children.length, 0);
  });
});
