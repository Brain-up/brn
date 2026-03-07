import { module, test } from 'qunit';
import { visit, currentURL, settled } from '@ember/test-helpers';
import { setupMSW } from '../../helpers/msw';
import { setupApplicationTest } from 'ember-qunit';
import { authenticateSession } from 'ember-simple-auth/test-support';

import {
  getUnaccessibleTaskScenario,
  getUnaccessibleExerciseScenario,
} from './test-support/helpers';

module('Acceptance | unaccessible routes', function (hooks) {
  setupApplicationTest(hooks);
  setupMSW(hooks);

  hooks.beforeEach(async () => {
    await authenticateSession();
  });

  test('visiting unaccessible task redirects to the first task', async function (assert) {
    getUnaccessibleTaskScenario();

    await visit('/groups/1/series/1/subgroup/1/exercise/1/task/1');

    assert.dom('[data-test-task-player]').exists('task player is shown');

    const firstTaskUrl = currentURL();

    // Task 2 is not interactable (task 1 hasn't been completed), so the
    // route guard in task.ts afterModel should redirect back to task 1.
    try {
      await visit('/groups/1/series/1/subgroup/1/exercise/1/task/2');
    } catch (_e) {
      // TransitionAborted is expected when the route redirects
      await settled();
    }

    assert.equal(currentURL(), firstTaskUrl, 'URL stays on the first task');
  });

  test('visiting task that is not in the current exercise redirects to the correct task', async function (assert) {
    getUnaccessibleExerciseScenario();

    await visit('/groups/1/series/1/subgroup/1/exercise/2/task/3');

    assert.dom('[data-test-task-player]').exists('task player is shown');

    const correctTaskUrl = currentURL();

    // Task 1 is not in exercise 2 (exercise 2 only has task 3), so the
    // route guard in task.ts model() should redirect to exercise 2's first task.
    try {
      await visit('/groups/1/series/1/subgroup/1/exercise/2/task/1');
    } catch (_e) {
      // TransitionAborted is expected when the route redirects
      await settled();
    }

    assert.equal(currentURL(), correctTaskUrl, 'URL stays on the correct task');
  });

  // 'visiting unaccessible series' test was removed: no route-level guard for
  // series accessibility exists in the codebase. The exercise availability
  // redirect is explicitly disabled during testing (via isTesting()), and
  // series routes have no access-control logic.
});
