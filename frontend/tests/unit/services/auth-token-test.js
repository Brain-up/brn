import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';

module('Unit | Service | auth-token', function (hooks) {
  setupTest(hooks);

  test('token returns empty string when session has no auth data', function (assert) {
    class MockSession extends Service {
      isAuthenticated = false;
      data = {};
    }
    this.owner.register('service:session', MockSession);

    const service = this.owner.lookup('service:auth-token');
    assert.strictEqual(service.token, '', 'token is empty when no session data');
  });

  test('token returns empty string when auth data is partially present', function (assert) {
    class MockSession extends Service {
      isAuthenticated = true;
      data = { authenticated: { user: {} } };
    }
    this.owner.register('service:session', MockSession);

    const service = this.owner.lookup('service:auth-token');
    assert.strictEqual(
      service.token,
      '',
      'token is empty when stsTokenManager is missing',
    );
  });

  test('token extracts accessToken from deep session path', function (assert) {
    class MockSession extends Service {
      isAuthenticated = true;
      data = {
        authenticated: {
          user: {
            stsTokenManager: {
              accessToken: 'firebase-token-abc',
            },
          },
        },
      };
    }
    this.owner.register('service:session', MockSession);

    const service = this.owner.lookup('service:auth-token');
    assert.strictEqual(service.token, 'firebase-token-abc');
  });

  test('headers returns empty object when not authenticated', function (assert) {
    class MockSession extends Service {
      isAuthenticated = false;
      data = {};
    }
    this.owner.register('service:session', MockSession);

    const service = this.owner.lookup('service:auth-token');
    assert.deepEqual(service.headers, {}, 'no headers when unauthenticated');
  });

  test('headers returns Authorization Bearer header when authenticated', function (assert) {
    class MockSession extends Service {
      isAuthenticated = true;
      data = {
        authenticated: {
          user: {
            stsTokenManager: {
              accessToken: 'my-token-123',
            },
          },
        },
      };
    }
    this.owner.register('service:session', MockSession);

    const service = this.owner.lookup('service:auth-token');
    assert.deepEqual(service.headers, {
      Authorization: 'Bearer my-token-123',
    });
  });

  test('headers does not include Authorization when authenticated but token is empty', function (assert) {
    class MockSession extends Service {
      isAuthenticated = false;
      data = {
        authenticated: {
          user: {},
        },
      };
    }
    this.owner.register('service:session', MockSession);

    const service = this.owner.lookup('service:auth-token');
    assert.deepEqual(
      service.headers,
      {},
      'empty headers when isAuthenticated is false even with partial data',
    );
  });
});
