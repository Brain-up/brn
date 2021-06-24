import { module, skip } from 'qunit';
import { currentURL } from '@ember/test-helpers';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';
import { setupApplicationTest } from 'ember-qunit';
import pageObject from './test-support/page-object';
import { authenticateSession } from 'ember-simple-auth/test-support';

import {
  getUnaccessibleTaskScenario,
  getUnaccessibleExerciseScenario,
  getUnaccessibleSeriesScenario,
} from './test-support/helpers';

module('Acceptance | unaccessible routes', function (hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  hooks.beforeEach(async () => {
    await authenticateSession();
  });

  skip('visiting unaccessible task', async function (assert) {
    getUnaccessibleTaskScenario();

    await pageObject.goToAccessibleTask();

    assert.ok(pageObject.taskPlayerIsPresent, 'task player is shown');

    const firstSiblingUrl = currentURL();

    // await pageObject.goToUnaccessibleTask();

    assert.equal(currentURL(), firstSiblingUrl);
  });

  skip('visiting task that is not in the current exercise( using non-first exercise )', async function (assert) {
    getUnaccessibleExerciseScenario();

    await pageObject.goToRightTaskInTheExercise();

    assert.ok(pageObject.taskPlayerIsPresent, 'task player is shown');

    const firstSiblingUrl = currentURL();

    await pageObject.goToWrongTaskInTheExercise();

    assert.equal(currentURL(), firstSiblingUrl);
  });

  skip('visiting unaccessible series', async function (assert) {
    getUnaccessibleSeriesScenario();

    await pageObject.goToAccessibleSeries();

    assert.ok(pageObject.taskPlayerIsPresent, 'task player is shown');

    await pageObject.goToUnaccessibleSeries();

    assert.equal(currentURL(), '/groups/1');
  });
});
