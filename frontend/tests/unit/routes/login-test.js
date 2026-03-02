import { module, test } from 'qunit';
import { visit, currentURL, settled } from '@ember/test-helpers';
import { setupApplicationTest } from 'ember-qunit';
import {
  currentSession,
  authenticateSession,
  invalidateSession,
} from 'ember-simple-auth/test-support';
import { setupMSW } from '../../helpers/msw';

module('Acceptance | app test', function (hooks) {
  setupApplicationTest(hooks);
  setupMSW(hooks);

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
    try {
      await visit('/');
    } catch (_e) {
      // TransitionAborted is expected: IndexRoute redirects authenticated users to /groups
    }
    await settled();

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
