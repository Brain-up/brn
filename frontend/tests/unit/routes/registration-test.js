import { module, test } from 'qunit';
import { visit, currentURL } from '@ember/test-helpers';
import { setupApplicationTest } from 'ember-qunit';
import { invalidateSession } from 'ember-simple-auth/test-support';
import { click } from '@ember/test-helpers';

module('Acceptance | app test', function(hooks) {
  setupApplicationTest(hooks);

  test('/Registration redirects to index if user is alread logged in', async function(assert) {
    await visit('/login');

    assert.equal(currentURL(), '/login');

    await click('[data-test-registration-form]');

    await visit('/rigistration');

    assert.equal(currentURL(), '/rigistration');
  });

  test('/Registration protected redirects to /registration if user is not logged in', async function(assert) {
    await visit('/login');

    await click('[data-test-registration-form]');

    await visit('/rigistration');

    await invalidateSession();

    await visit('/groups');

    assert.equal(currentURL(), '/login');
  });
});
