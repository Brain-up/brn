import { module, test } from 'qunit';
import { AuthHandler } from 'brn/handlers/auth-handler';

module('Unit | Handler | auth-handler', function () {
  test('sets Content-Type header to application/json', async function (assert) {
    const handler = new AuthHandler({
      headers: {},
    });

    const context = {
      request: {
        headers: new Headers(),
      },
    };

    await handler.request(context, async (req) => {
      assert.strictEqual(
        req.headers.get('Content-Type'),
        'application/json',
      );
      return {};
    });
  });

  test('injects Authorization header from authToken service', async function (assert) {
    const handler = new AuthHandler({
      headers: {
        Authorization: 'Bearer test-token-123',
      },
    });

    const context = {
      request: {
        headers: new Headers(),
      },
    };

    await handler.request(context, async (req) => {
      assert.strictEqual(
        req.headers.get('Authorization'),
        'Bearer test-token-123',
      );
      return {};
    });
  });

  test('injects multiple headers from authToken service', async function (assert) {
    const handler = new AuthHandler({
      headers: {
        Authorization: 'Bearer abc',
        'X-Custom-Header': 'custom-value',
      },
    });

    const context = {
      request: {
        headers: new Headers(),
      },
    };

    await handler.request(context, async (req) => {
      assert.strictEqual(req.headers.get('Authorization'), 'Bearer abc');
      assert.strictEqual(req.headers.get('X-Custom-Header'), 'custom-value');
      assert.strictEqual(req.headers.get('Content-Type'), 'application/json');
      return {};
    });
  });

  test('preserves existing headers on the request', async function (assert) {
    const handler = new AuthHandler({
      headers: {
        Authorization: 'Bearer token',
      },
    });

    const context = {
      request: {
        headers: new Headers({ 'Accept-Language': 'en-US' }),
      },
    };

    await handler.request(context, async (req) => {
      assert.strictEqual(req.headers.get('Accept-Language'), 'en-US');
      assert.strictEqual(req.headers.get('Authorization'), 'Bearer token');
      return {};
    });
  });

  test('calls next with the modified request and returns its result', async function (assert) {
    const handler = new AuthHandler({
      headers: {},
    });

    const context = {
      request: {
        headers: new Headers(),
      },
    };

    const result = await handler.request(context, async () => {
      return { data: 'response-data' };
    });

    assert.deepEqual(result, { data: 'response-data' });
  });

  test('works with empty auth headers', async function (assert) {
    const handler = new AuthHandler({
      headers: {},
    });

    const context = {
      request: {
        headers: new Headers(),
      },
    };

    await handler.request(context, async (req) => {
      assert.strictEqual(
        req.headers.get('Content-Type'),
        'application/json',
        'Content-Type is always set',
      );
      assert.strictEqual(
        req.headers.get('Authorization'),
        null,
        'no Authorization header when none provided',
      );
      return {};
    });
  });
});
