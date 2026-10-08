import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';

module('Unit | Route | group', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let route = this.owner.lookup('route:group');
    assert.ok(route);
  });

  test('model returns composite { group, series } object', async function (assert) {
    const mockGroup = { id: '1', name: 'Test Group' };
    const mockSeries = [
      { id: '3', name: 'Third' },
      { id: '1', name: 'First' },
      { id: '2', name: 'Second' },
    ];

    class MockStore extends Service {
      findRecord() {
        return Promise.resolve(mockGroup);
      }
      query() {
        return Promise.resolve(mockSeries);
      }
      // Required by WarpDrive store interface
      cacheKeyManager = { getOrCreateRecordIdentifier: () => ({}) };
    }

    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:group');
    const result = await route.model({ group_id: '1' });

    assert.ok(result.group, 'has group property');
    assert.ok(result.series, 'has series property');
    assert.strictEqual(result.group.id, '1');
    // series should be sorted by id
    assert.strictEqual(result.series[0].id, '1');
    assert.strictEqual(result.series[1].id, '2');
    assert.strictEqual(result.series[2].id, '3');
  });

  test('redirect goes to groups when no series', function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group');
    // Stub the router directly on the route instance
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };

    route.redirect(
      { group: { id: '1' }, series: [] },
      { to: { name: 'group.index' } },
    );
    assert.strictEqual(transitionArgs[0], 'groups');
  });

  test('redirect goes to first series on group.index', function (assert) {
    let transitionArgs = [];

    const route = this.owner.lookup('route:group');
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };

    route.redirect(
      {
        group: { id: '5' },
        series: [{ id: '10' }, { id: '20' }],
      },
      { to: { name: 'group.index' } },
    );
    assert.strictEqual(transitionArgs[0], 'group.series.index');
    assert.strictEqual(transitionArgs[1], '5');
    assert.strictEqual(transitionArgs[2], '10');
  });

  test('redirect does nothing when not on group.index', function (assert) {
    let transitioned = false;

    const route = this.owner.lookup('route:group');
    route.router = {
      transitionTo() {
        transitioned = true;
      },
    };

    route.redirect(
      {
        group: { id: '5' },
        series: [{ id: '10' }],
      },
      { to: { name: 'group.series.index' } },
    );
    assert.false(transitioned, 'no redirect on non-index routes');
  });

  test('model propagates findRecord errors', async function (assert) {
    class MockStore extends Service {
      findRecord() {
        return Promise.reject(new Error('network error'));
      }
      query() {
        return Promise.resolve([]);
      }
      cacheKeyManager = { getOrCreateRecordIdentifier: () => ({}) };
    }

    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:group');

    try {
      await route.model({ group_id: '1' });
      assert.ok(false, 'should have thrown');
    } catch (e) {
      assert.strictEqual(e.message, 'network error');
    }
  });

  test('model propagates query errors', async function (assert) {
    class MockStore extends Service {
      findRecord() {
        return Promise.resolve({ id: '1' });
      }
      query() {
        return Promise.reject(new Error('query failed'));
      }
      cacheKeyManager = { getOrCreateRecordIdentifier: () => ({}) };
    }

    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:group');

    try {
      await route.model({ group_id: '1' });
      assert.ok(false, 'should have thrown');
    } catch (e) {
      assert.strictEqual(e.message, 'query failed');
    }
  });

  test('model passes group_id directly to both findRecord and query', async function (assert) {
    let findRecordArgs = [];
    let queryArgs = [];

    class MockStore extends Service {
      findRecord(type, id) {
        findRecordArgs = [type, id];
        return Promise.resolve({ id });
      }
      query(type, params) {
        queryArgs = [type, params];
        return Promise.resolve([]);
      }
      cacheKeyManager = { getOrCreateRecordIdentifier: () => ({}) };
    }

    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:group');
    await route.model({ group_id: '42' });

    assert.deepEqual(findRecordArgs, ['group', '42'], 'findRecord receives group_id');
    assert.strictEqual(queryArgs[0], 'series');
    assert.strictEqual(queryArgs[1].groupId, '42', 'query receives group_id as groupId');
  });

  test('redirect with bare GroupModel updates controller model to composite format', async function (assert) {
    const mockSeries = [{ id: '3' }, { id: '1' }];

    class MockStore extends Service {
      query() {
        return Promise.resolve(mockSeries);
      }
      cacheKeyManager = { getOrCreateRecordIdentifier: () => ({}) };
    }

    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:group');
    const controller = this.owner.lookup('controller:group');
    let transitionArgs = [];
    route.router = {
      transitionTo(...args) {
        transitionArgs = args;
      },
    };

    // Pass a bare GroupModel (no 'group'/'series' keys in composite format)
    const bareGroup = { id: '7', name: 'Bare' };
    await route.redirect(bareGroup, { to: { name: 'group.index' } });

    // Controller model should now be the composite format
    assert.ok(controller.model.group, 'controller model has group');
    assert.ok(controller.model.series, 'controller model has series');
    assert.strictEqual(controller.model.group.id, '7', 'group is correct');
    assert.strictEqual(controller.model.series.length, 2, 'series are populated');
    // Series should be sorted by id
    assert.strictEqual(controller.model.series[0].id, '1', 'series sorted by id');

    // Should still redirect to first series
    assert.strictEqual(transitionArgs[0], 'group.series.index');
    assert.strictEqual(transitionArgs[1], '7');
    assert.strictEqual(transitionArgs[2], '1');
  });

  test('model handles null series from query', async function (assert) {
    class MockStore extends Service {
      findRecord() {
        return Promise.resolve({ id: '1' });
      }
      query() {
        return Promise.resolve(null);
      }
      cacheKeyManager = { getOrCreateRecordIdentifier: () => ({}) };
    }

    this.owner.register('service:store', MockStore);

    const route = this.owner.lookup('route:group');
    const result = await route.model({ group_id: '1' });

    assert.ok(result.group, 'has group');
    assert.deepEqual(result.series, [], 'series defaults to empty array for null');
  });
});
