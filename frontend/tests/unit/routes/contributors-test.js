import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';

module('Unit | Route | contributors', function (hooks) {
  setupTest(hooks);

  test('model filters out inactive contributors', async function (assert) {
    class MockStore extends Service {
      findAll() {
        return Promise.resolve([
          { isActive: true, contribution: 10, kind: 'DEVELOPER' },
          { isActive: false, contribution: 20, kind: 'DEVELOPER' },
          { isActive: true, contribution: 5, kind: 'QA' },
        ]);
      }
    }
    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:contributors');
    const result = await route.model();

    assert.strictEqual(result.length, 2, 'only active contributors remain');
    assert.true(
      result.every((c) => c.isActive),
      'all returned contributors are active',
    );
  });

  test('model sorts by contribution descending', async function (assert) {
    class MockStore extends Service {
      findAll() {
        return Promise.resolve([
          { isActive: true, contribution: 5, kind: 'DEVELOPER' },
          { isActive: true, contribution: 20, kind: 'QA' },
          { isActive: true, contribution: 10, kind: 'DESIGNER' },
        ]);
      }
    }
    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:contributors');
    const result = await route.model();

    assert.deepEqual(
      result.map((c) => c.contribution),
      [20, 10, 5],
      'sorted by contribution descending',
    );
  });

  test('model filters by allowed kinds only', async function (assert) {
    class MockStore extends Service {
      findAll() {
        return Promise.resolve([
          { isActive: true, contribution: 10, kind: 'DEVELOPER' },
          { isActive: true, contribution: 8, kind: 'SPECIALIST' },
          { isActive: true, contribution: 6, kind: 'QA' },
          { isActive: true, contribution: 4, kind: 'DESIGNER' },
          { isActive: true, contribution: 2, kind: 'OTHER' },
          { isActive: true, contribution: 1, kind: 'AUTOTESTER' },
          { isActive: true, contribution: 3, kind: 'UNKNOWN_TYPE' },
        ]);
      }
    }
    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:contributors');
    const result = await route.model();
    const kinds = result.map((c) => c.kind);

    assert.false(kinds.includes('SPECIALIST'), 'SPECIALIST excluded from contributors');
    assert.false(kinds.includes('UNKNOWN_TYPE'), 'unknown kinds excluded');
    assert.strictEqual(result.length, 5, 'only DEVELOPER, QA, DESIGNER, OTHER, AUTOTESTER');
  });

  test('model returns empty array when no contributors match', async function (assert) {
    class MockStore extends Service {
      findAll() {
        return Promise.resolve([
          { isActive: false, contribution: 10, kind: 'DEVELOPER' },
          { isActive: true, contribution: 5, kind: 'SPECIALIST' },
        ]);
      }
    }
    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:contributors');
    const result = await route.model();

    assert.strictEqual(result.length, 0, 'no contributors when none match all filters');
  });
});
