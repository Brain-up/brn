import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Service | store', function (hooks) {
  setupTest(hooks);

  test('requestManager has handlers configured (AuthHandler, BrnApiHandler, Fetch, CacheHandler)', function (assert) {
    const store = this.owner.lookup('service:store');
    const rm = store.requestManager;
    assert.ok(rm, 'requestManager is initialized');
    assert.ok(rm._handlers, 'requestManager has handlers configured');
    assert.true(rm._handlers.length >= 3, 'at least 3 request handlers registered (auth, api, fetch + cache)');
  });

  test('createRecord creates a record with the given type and attributes', function (assert) {
    const store = this.owner.lookup('service:store');
    const record = store.createRecord('group', { name: 'Test Group', locale: 'en-us' });
    assert.strictEqual(record.name, 'Test Group');
    assert.strictEqual(record.locale, 'en-us');
  });

  test('findRecord stringifies numeric IDs', async function (assert) {
    assert.expect(2);
    const store = this.owner.lookup('service:store');

    // Intercept the request method to inspect the request payload
    const origRequest = store.request.bind(store);
    store.request = function (req) {
      assert.strictEqual(req.op, 'findRecord', 'operation is findRecord');
      assert.strictEqual(
        req.data.record.id,
        '42',
        'numeric ID 42 is stringified to "42"',
      );
      // Return a resolved promise to prevent actual network call
      return Promise.resolve({ content: { data: null } });
    };

    try {
      await store.findRecord('group', 42);
    } catch {
      // ignore errors from downstream processing
    } finally {
      store.request = origRequest;
    }
  });

  test('query passes type and query params to request', async function (assert) {
    assert.expect(3);
    const store = this.owner.lookup('service:store');

    const origRequest = store.request.bind(store);
    store.request = function (req) {
      assert.strictEqual(req.op, 'query', 'operation is query');
      assert.strictEqual(req.data.type, 'series', 'type is passed through');
      assert.deepEqual(req.data.query, { groupId: '5' }, 'query params are passed through');
      return Promise.resolve({ content: { data: [] } });
    };

    try {
      await store.query('series', { groupId: '5' });
    } catch {
      // ignore
    } finally {
      store.request = origRequest;
    }
  });

  test('findAll passes type to request', async function (assert) {
    assert.expect(2);
    const store = this.owner.lookup('service:store');

    const origRequest = store.request.bind(store);
    store.request = function (req) {
      assert.strictEqual(req.op, 'findAll', 'operation is findAll');
      assert.strictEqual(req.data.type, 'contributor', 'type is passed through');
      return Promise.resolve({ content: { data: [] } });
    };

    try {
      await store.findAll('contributor');
    } catch {
      // ignore
    } finally {
      store.request = origRequest;
    }
  });

  test('schemas are registered (can create records for known types)', function (assert) {
    const store = this.owner.lookup('service:store');
    // These should not throw — proves schemas are registered
    const group = store.createRecord('group', {});
    assert.ok(group, 'group schema registered');

    const signal = store.createRecord('signal', {});
    assert.ok(signal, 'signal schema registered');

    const exercise = store.createRecord('exercise', {});
    assert.ok(exercise, 'exercise schema registered');
  });
});
