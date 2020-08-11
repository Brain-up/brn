import { module, test } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import { getServerResponses, chooseAnswer } from '../general-helpers';
import { getTestData } from './test-support/data-storage';
import pageObject from './test-support/page-object';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';
import { authenticateSession  } from 'ember-simple-auth/test-support';


module('Acceptance | exercises availability', function(hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  hooks.beforeEach(async function() {
    await authenticateSession();
    getServerResponses(getTestData());
  });

  test('shows the right number of exercise groups', async function(assert) {
    await pageObject.goToSeriesPage();

    assert.dom('[data-test-series-navigation-header]').exists({ count: 2 });
    assert.dom('[data-test-exercises-name-group]').exists({ count: 2 });
    assert.dom('[data-test-series-navigation-list-link]').exists({ count: 4 });
  });

  test('first exercices in the name group is available by default', async function(assert) {
    await pageObject.goToSeriesPage();

    assert.dom('[data-test-exercise-level="1"]').exists({ count: 2 });
    assert
      .dom(
        '[data-test-exercise-level="1"][data-test-exercise-name="exercise 1"]',
      )
      .hasNoAttribute('disabled');
    assert
      .dom(
        '[data-test-exercise-level="1"][data-test-exercise-name="exercise 2"]',
      )
      .hasNoAttribute('disabled');
    assert
      .dom(
        '[data-test-exercise-level="2"][data-test-exercise-name="exercise 1"]',
      )
      .hasAttribute('disabled');
    assert
      .dom(
        '[data-test-exercise-level="2"][data-test-exercise-name="exercise 2"]',
      )
      .hasAttribute('disabled');
  });

  test('marks available exercises withing a name group if previous is completed', async function(assert) {
    await pageObject.goToSeriesPage();
    await pageObject.goToFirstExercisePage();
    await pageObject.startTask();
    await chooseAnswer('test option');

    assert.dom('[data-test-exercise-level="1"]').exists({ count: 2 });
    assert
      .dom(
        '[data-test-exercise-level="1"][data-test-exercise-name="exercise 1"]',
      )
      .hasNoAttribute('disabled');

    assert
      .dom(
        '[data-test-exercise-level="2"][data-test-exercise-name="exercise 1"]',
      )
      .hasNoAttribute('disabled');
  });
});
