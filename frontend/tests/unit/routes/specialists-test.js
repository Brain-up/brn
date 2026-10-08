import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';

module('Unit | Route | specialists', function (hooks) {
  setupTest(hooks);

  test('model returns only SPECIALIST kind', async function (assert) {
    class MockStore extends Service {
      findAll() {
        return Promise.resolve([
          { isActive: true, contribution: 10, kind: 'DEVELOPER' },
          { isActive: true, contribution: 20, kind: 'SPECIALIST' },
          { isActive: true, contribution: 5, kind: 'QA' },
          { isActive: true, contribution: 15, kind: 'SPECIALIST' },
        ]);
      }
    }
    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:specialists');
    const result = await route.model();

    assert.strictEqual(result.length, 2, 'only specialists returned');
    assert.true(
      result.every((c) => c.kind === 'SPECIALIST'),
      'all entries are SPECIALIST',
    );
  });

  test('model filters out inactive specialists', async function (assert) {
    class MockStore extends Service {
      findAll() {
        return Promise.resolve([
          { isActive: true, contribution: 10, kind: 'SPECIALIST' },
          { isActive: false, contribution: 20, kind: 'SPECIALIST' },
          { isActive: true, contribution: 5, kind: 'SPECIALIST' },
        ]);
      }
    }
    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:specialists');
    const result = await route.model();

    assert.strictEqual(result.length, 2, 'inactive specialist filtered out');
    assert.true(
      result.every((c) => c.isActive),
      'all returned specialists are active',
    );
  });

  test('model sorts by contribution descending', async function (assert) {
    class MockStore extends Service {
      findAll() {
        return Promise.resolve([
          { isActive: true, contribution: 5, kind: 'SPECIALIST' },
          { isActive: true, contribution: 20, kind: 'SPECIALIST' },
          { isActive: true, contribution: 10, kind: 'SPECIALIST' },
        ]);
      }
    }
    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:specialists');
    const result = await route.model();

    assert.deepEqual(
      result.map((c) => c.contribution),
      [20, 10, 5],
      'sorted by contribution descending',
    );
  });

  test('model returns empty array when no specialists exist', async function (assert) {
    class MockStore extends Service {
      findAll() {
        return Promise.resolve([
          { isActive: true, contribution: 10, kind: 'DEVELOPER' },
        ]);
      }
    }
    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:specialists');
    const result = await route.model();

    assert.strictEqual(result.length, 0, 'no results when no specialists');
  });
});
