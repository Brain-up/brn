/* eslint-disable @typescript-eslint/no-empty-function */
import { module, skip, test } from 'qunit';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';
import { setupApplicationTest } from 'ember-qunit';
import pageObject from './test-support/page-object';
import { setupAfterPageVisit } from './test-support/helpers';
import { getServerResponses, chooseAnswer } from '../general-helpers';
import { click, waitFor } from '@ember/test-helpers';
import customTimeout from 'brn/utils/custom-timeout';
import { currentURL } from '@ember/test-helpers';
import { getData } from './test-support/data-storage';
import { authenticateSession } from 'ember-simple-auth/test-support';
import { getOwner } from '@ember/application';

module('Acceptance | tasks flow', function (hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  hooks.beforeEach(async () => {
    await authenticateSession();
    getServerResponses(getData());
  });

  test('has a start task button if the task is not started yet', async function (assert) {
    await pageObject.goToFirstTask();
    assert.dom('[data-test-start-task-button]').exists();

    await pageObject.startTask();

    assert.dom('[data-test-start-task-button]').doesNotExist();
  });

  skip('shows regret widget if answer is wrong and a word image if right', async function (assert) {
    await pageObject.goToFirstTask();

    const { wrongAnswer } = setupAfterPageVisit();

    await pageObject.startTask();

    await chooseAnswer(wrongAnswer.word);

    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-isnt-correct');
  });

  test('goest to next task after a right answer picture', async function (assert) {
    await pageObject.goToFirstTask();

    setupAfterPageVisit();

    await pageObject.startTask();

    const audio = getOwner(this).lookup('service:audio');

    for (let i = 0; i < 15; i++) {
      await new Promise((resolve) => setTimeout(resolve, 100));
      await chooseAnswer(audio._lastText);
    }

    await waitFor('[data-test-task-id="2"]');
    assert.dom('[data-test-task-id="2"]').exists();
  });

  test('sends a POST request to "study-history" after exercise completed', async function (assert) {
    assert.expect(1);

    /* eslint-disable no-undef */
    server.post('/study-history', function (request) {
      assert.ok(true, 'sends a post request');
      return { id: 1, ...JSON.parse(request.requestBody) };
    });

    await pageObject.goToFirstTaskSecondExercise();

    const audio = getOwner(this).lookup('service:audio');

    setupAfterPageVisit();

    await pageObject.startTask();

    for (let i = 0; i < 15; i++) {
      await new Promise((resolve) => setTimeout(resolve, 100));
      await chooseAnswer(audio._lastText);
    }

    await customTimeout();

    await click('[data-test-continue]');

    // const targetTask2 = setupAfterPageVisit().targetTask;
    // await waitFor('[data-test-task-answer-option]');
    // await chooseAnswer(targetTask2.correctAnswer.word);
    await customTimeout();
  });

  skip('shows a complete victory widget after exercise completed and goes to series route', async function (assert) {
    /* eslint-disable no-undef */
    server.put('exercises/1', function () {});

    await pageObject.goToFirstTask();

    const audio = getOwner(this).lookup('service:audio');

    setupAfterPageVisit();

    await pageObject.startTask();

    for (let i = 0; i < 15; i++) {
      await new Promise((resolve) => setTimeout(resolve, 100));
      await chooseAnswer(audio._lastText);
    }

    // const rightAnswerOneNotificationPromise = waitFor('[data-test-right-answer-notification]', {
    //   timeout: 1000,
    // });

    // await rightAnswerOneNotificationPromise;

    await waitFor('[data-test-task-id="2"]');

    setupAfterPageVisit().targetTask;

    // const rightAnswerTwoNotificationPromise = waitFor('[data-test-right-answer-notification]', {
    //   timeout: 1000,
    // });

    for (let i = 0; i < 15; i++) {
      await new Promise((resolve) => setTimeout(resolve, 100));
      console.log(audio._lastText);

      await chooseAnswer(audio._lastText);
      console.log(audio._lastText);
    }

    // await waitFor('[data-test-exercise-stats]');

    await click('[data-test-continue]');

    await customTimeout();
    await customTimeout();

    assert.equal(currentURL(), '/groups/1/series/1/subgroup/1');
  });
});
