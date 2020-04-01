import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import Service from '@ember/service';
module('Unit | Adapter | application', function(hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('it exists', function(assert) {
    let adapter = this.owner.lookup('adapter:application');
    assert.ok(adapter);
  });

  test('it return proper autorization headers isAuthenticated = false', function(assert) {
    let adapter = this.owner.lookup('adapter:application');
    class MockSession extends Service {
      isAuthenticated = false
    }

    this.owner.register('service:session', MockSession);

    assert.deepEqual(adapter.headers, {});
  });

  test('it return proper autorization headers isAuthenticated = true', function(assert) {
    let adapter = this.owner.lookup('adapter:application');
    class MockSession extends Service {
      isAuthenticated = true
      data = {
        authenticated: {
          access_token: '42'
        }
      }
    }

    this.owner.register('service:session', MockSession);

    assert.deepEqual(adapter.headers, {'Authorization': 'Basic 42'});
  });
});
