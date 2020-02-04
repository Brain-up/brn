import { module, test, skip } from 'qunit';
import { currentURL } from '@ember/test-helpers';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';
import { setupApplicationTest } from 'ember-qunit';
import pageObject from './test-support/page-object';
import unaccessibleExerciseScenario from '../../../mirage/scenarios/unaccessible-exercise';
import unaccessibleTaskScenario from '../../../mirage/scenarios/unaccessible-task';
import unaccessibleSeriesScenario from '../../../mirage/scenarios/unaccessible-series';

module('Acceptance | unaccessible routes', function(hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  test('visiting unaccessible task', async function(assert) {
    /* eslint-disable no-undef */
    unaccessibleTaskScenario(server);

    await pageObject.goToAccessibleTask();

    assert.ok(pageObject.taskPlayerIsPresent, 'task player is shown');

    const firstSiblingUrl = currentURL();

    await pageObject.goToUnaccessibleTask();

    assert.equal(currentURL(), firstSiblingUrl);
  });

  test('visiting task that is not in the current exercise( using non-first exercise )', async function(assert) {
    /* eslint-disable no-undef */
    unaccessibleExerciseScenario(server);

    await pageObject.goToRightTaskInTheExercise();

    assert.ok(pageObject.taskPlayerIsPresent, 'task player is shown');

    const firstSiblingUrl = currentURL();

    await pageObject.goToWrongTaskInTheExercise();

    assert.equal(currentURL(), firstSiblingUrl);
  });

  skip('visiting unaccessible series', async function(assert) {
    /* eslint-disable no-undef */
    unaccessibleSeriesScenario(server);

    await pageObject.goToAccessibleSeries();

    assert.ok(pageObject.taskPlayerIsPresent, 'task player is shown');

    await pageObject.goToUnaccessibleSeries();

    assert.equal(currentURL(), '/groups/1');
  });
});
