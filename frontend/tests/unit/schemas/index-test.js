import { module, test } from 'qunit';
import { ALL_SCHEMAS, ALL_EXTENSIONS } from 'brn/schemas/index';

module('Unit | Schema | index', function () {
  test('ALL_SCHEMAS contains the expected number of schemas', function (assert) {
    assert.strictEqual(ALL_SCHEMAS.length, 14);
  });

  test('ALL_EXTENSIONS contains the expected number of extensions', function (assert) {
    assert.strictEqual(ALL_EXTENSIONS.length, 10);
  });

  test('ALL_SCHEMAS contains all expected schema types', function (assert) {
    const types = ALL_SCHEMAS.map((s) => s.type);
    assert.true(types.includes('signal'), 'includes signal');
    assert.true(types.includes('headphone'), 'includes headphone');
    assert.true(types.includes('user-daily-time-table-statistics'), 'includes user-daily-time-table-statistics');
    assert.true(types.includes('user-weekly-statistics'), 'includes user-weekly-statistics');
    assert.true(types.includes('user-yearly-statistics'), 'includes user-yearly-statistics');
    assert.true(types.includes('contributor'), 'includes contributor');
    assert.true(types.includes('series'), 'includes series');
    assert.true(types.includes('subgroup'), 'includes subgroup');
    assert.true(types.includes('group'), 'includes group');
    assert.true(types.includes('exercise'), 'includes exercise');
    assert.true(types.includes('task'), 'includes task');
    assert.true(types.includes('task/signal'), 'includes task/signal');
    assert.true(types.includes('task/single-simple-words'), 'includes task/single-simple-words');
    assert.true(types.includes('task/words-sequences'), 'includes task/words-sequences');
  });

  test('ALL_EXTENSIONS contains all expected extension names', function (assert) {
    const names = ALL_EXTENSIONS.map((e) => e.name);
    assert.true(names.includes('user-weekly-statistics-ext'), 'includes user-weekly-statistics-ext');
    assert.true(names.includes('user-yearly-statistics-ext'), 'includes user-yearly-statistics-ext');
    assert.true(names.includes('task-single-simple-words-ext'), 'includes task-single-simple-words-ext');
    assert.true(names.includes('task-words-sequences-ext'), 'includes task-words-sequences-ext');
  });

  test('all schemas have a type property', function (assert) {
    ALL_SCHEMAS.forEach((schema) => {
      assert.ok(schema.type, `schema has type: ${schema.type}`);
    });
  });

  test('all extensions have kind "object"', function (assert) {
    ALL_EXTENSIONS.forEach((ext) => {
      assert.strictEqual(ext.kind, 'object', `${ext.name} has kind "object"`);
    });
  });
});
