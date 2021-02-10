import { module, test } from 'qunit';
import { visit, currentURL } from '@ember/test-helpers';
import { setupApplicationTest } from 'ember-qunit';
import {
  currentSession,
  authenticateSession,
  invalidateSession,
} from 'ember-simple-auth/test-support';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';

module('Acceptance | app test', function (hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  // hooks.beforeEach(async function () {
  //   // this.server.get('users/current', () => {
  //   //   return {
  //   //     data: [
  //   //       { id: 1, name: "admin", email: "admin@admin.com", avatar: "1" }
  //   //     ]
  //   //   }
  //   // });
  // });

  test('/login redirects to index if user is alread logged in', async function (assert) {
    await authenticateSession({
      authToken: '12345',
      otherData: 'some-data',
    });
    await visit('/');

    assert.equal(currentURL(), '/groups');

    let sessionData = currentSession().get('data.authenticated');
    assert.equal(sessionData.authToken, '12345');
    assert.equal(sessionData.otherData, 'some-data');
  });

  test('/protected redirects to /login if user is not logged in', async function (assert) {
    await invalidateSession();

    await visit('/groups');

    assert.equal(currentURL(), '/login');
  });
});
