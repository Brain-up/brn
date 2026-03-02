import { module, test, skip } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import { waitUntil, settled } from '@ember/test-helpers';
import {
  getServerResponses,
  chooseAnswer,
  continueAfterStats,
} from '../general-helpers';
import { getTestData } from './test-support/data-storage';
import pageObject from './test-support/page-object';
import { setupMSW } from '../../helpers/msw';
import { authenticateSession } from 'ember-simple-auth/test-support';

module('Acceptance | exercises availability', function (hooks) {
  setupApplicationTest(hooks);
  setupMSW(hooks);

  hooks.beforeEach(async function () {
    await authenticateSession();
    getServerResponses(getTestData());
  });

  test('shows the right number of exercise groups', async function (assert) {
    await pageObject.goToFirstSeriesPage();
    assert.dom('[data-test-series-navigation-header]').exists({ count: 2 });
    assert.dom('[data-test-exercises-name-group]').exists({ count: 2 });
    assert.dom('[data-test-series-navigation-list-link]').exists({ count: 4 });
  });

  test('first exercices in the name group is available by default', async function (assert) {
    await pageObject.goToFirstSeriesPage();
    // Wait for the keepLatestTask to resolve and update exercise availability
    await waitUntil(
      () => document.querySelector('[data-test-exercise-level="1"][data-test-exercise-name="exercise 1"]:not([aria-disabled])'),
      { timeout: 3000 },
    );
    await settled();
    assert.dom('[data-test-exercise-level="1"]').exists({ count: 2 });
    assert
      .dom(
        '[data-test-exercise-level="1"][data-test-exercise-name="exercise 1"]',
      )
      .doesNotHaveAttribute('aria-disabled');
    assert
      .dom(
        '[data-test-exercise-level="2"][data-test-exercise-name="exercise 1"]',
      )
      .hasAttribute('aria-disabled');

    //   await this.pauseTest();

    // await pageObject.goToSecondSeriesPage();
    // await this.pauseTest();
    // assert
    //   .dom(
    //     '[data-test-exercise-level="1"][data-test-exercise-name="exercise 2"]',
    //   )
    //   .hasNoAttribute('disabled');

    // assert
    //   .dom(
    //     '[data-test-exercise-level="2"][data-test-exercise-name="exercise 2"]',
    //   )
    //   .hasAttribute('disabled');
  });

  skip('marks available exercises withing a name group if previous is completed', async function (assert) {
    await pageObject.goToFirstSeriesPage();
    // Wait for exercise availability to be calculated
    await waitUntil(
      () => document.querySelector('[data-test-exercise-level="1"][data-test-exercise-name="exercise 1"]:not([aria-disabled])'),
      { timeout: 3000 },
    );
    await settled();

    assert
      .dom(
        '[data-test-exercise-level="2"][data-test-exercise-name="exercise 1"]',
      )
      .hasAttribute('aria-disabled');

    await pageObject.goToFirstExercisePage();
    await pageObject.startTask();

    const newData = getTestData();
    newData.availableExercises = ['1', '2'];
    getServerResponses(newData);

    await chooseAnswer('test option');
    await chooseAnswer('test option');
    await chooseAnswer('test option');

    await continueAfterStats();

    assert.dom('[data-test-exercise-level="1"]').exists({ count: 2 });
    assert
      .dom(
        '[data-test-exercise-level="1"][data-test-exercise-name="exercise 1"]',
      )
      .doesNotHaveAttribute('aria-disabled');

    assert
      .dom(
        '[data-test-exercise-level="2"][data-test-exercise-name="exercise 1"]',
      )
      .doesNotHaveAttribute('aria-disabled');
  });
});
