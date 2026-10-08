import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { setCloudBaseUrl } from 'brn/utils/file-url';

module('Unit | Schema | subgroup', function (hooks) {
  setupTest(hooks);

  hooks.afterEach(function () {
    setCloudBaseUrl('');
  });

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

  test('picture returns pictureUrl resolved through urlForImage', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {
      pictureUrl: 'test.png',
    });
    // urlForImage prepends / for relative paths without cloud URL
    assert.strictEqual(model.picture, '/test.png');
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

  // --- picture getter (urlForImage integration) ---

  test('picture resolves relative pictureUrl through urlForImage', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {
      pictureUrl: 'pictures/subgroup1.png',
    });
    // Without cloud URL, urlForImage prepends /
    assert.strictEqual(model.picture, '/pictures/subgroup1.png');
  });

  test('picture resolves absolute pictureUrl with cloud base url', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {
      pictureUrl: '/pictures/subgroup1.png',
    });
    assert.strictEqual(model.picture, 'https://cdn.example.com/pictures/subgroup1.png');
  });

  test('picture preserves http pictureUrl as-is', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {
      pictureUrl: 'https://other.com/subgroup1.png',
    });
    assert.strictEqual(model.picture, 'https://other.com/subgroup1.png');
  });

  test('picture skips cloud resolution for /public/ pictureUrl', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {
      pictureUrl: '/public/pictures/subgroup1.png',
    });
    assert.strictEqual(model.picture, '/public/pictures/subgroup1.png');
  });

  test('picture falls back to pictureUrl when urlForImage returns null', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {
      pictureUrl: null,
    });
    // urlForImage(null) returns null, fallback ?? self.pictureUrl is also null
    assert.strictEqual(model.picture, null);
  });

  test('picture resolves relative pictureUrl with cloud base url', function (assert) {
    setCloudBaseUrl('https://cdn.example.com');
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('subgroup', {
      pictureUrl: 'pictures/subgroup1.png',
    });
    assert.strictEqual(model.picture, 'https://cdn.example.com/pictures/subgroup1.png');
  });
});
