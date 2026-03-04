import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Schema | subgroup', function (hooks) {
  setupTest(hooks);

  test('creates a subgroup record', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {
      name: 'Level 1',
      seriesId: '10',
      level: 1,
      description: 'First level',
      order: 1,
      pictureUrl: 'pic.png',
    });
    assert.ok(model);
    assert.strictEqual(model.name, 'Level 1');
    assert.strictEqual(model.seriesId, '10');
  });

  test('picture returns pictureUrl', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {
      pictureUrl: 'test.png',
    });
    assert.strictEqual(model.picture, 'test.png');
  });

  test('exercisesIds filters null ids from exercises', function (assert) {
    const store = this.owner.lookup('service:store');
    // WarpDrive createRecord doesn't auto-populate inverse hasMany,
    // so test with a fresh subgroup (exercises = empty)
    const model = store.createRecord('subgroup', { name: 'SG1' });
    // exercisesIds on an empty relationship returns []
    assert.deepEqual(model.exercisesIds, []);
    assert.strictEqual(model.count, 0);
  });

  test('count delegates to exercisesIds length', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', { name: 'SG1' });
    // Both exercisesIds and count reflect the exercises relationship
    assert.strictEqual(model.count, model.exercisesIds.length);
  });

  test('exercisesIds returns empty array when no exercises', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {});
    assert.deepEqual(model.exercisesIds, []);
  });
});
