import { module, test } from 'qunit';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';
import { setupApplicationTest } from 'ember-qunit';
import pageObject from './test-support/page-object';
import { setupAfterPageVisit } from './test-support/helpers';
import { getServerResponses, chooseAnswer } from '../general-helpers';
import { click, settled, waitFor } from '@ember/test-helpers';
import { timeout } from 'ember-concurrency';
import customTimeout from 'brn/utils/custom-timeout';
import { currentURL } from '@ember/test-helpers';
import { getData } from './test-support/data-storage';
import { TIMINGS } from 'brn/utils/audio-api';
import { authenticateSession  } from 'ember-simple-auth/test-support';


module('Acceptance | tasks flow', function(hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  hooks.beforeEach(async () => {
    await authenticateSession();
    getServerResponses(getData());
  });

  test('has a start task button if the task is not started yet', async function(assert) {
    await pageObject.goToFirstTask();
    assert.dom('[data-test-start-task-button]').exists();

    await pageObject.startTask();

    assert.dom('[data-test-start-task-button]').doesNotExist();
  });

  test('shows regret widget if answer is wrong and a word image if right', async function(assert) {
    await pageObject.goToFirstTask();

    const { targetTask, wrongAnswer } = setupAfterPageVisit();

    await pageObject.startTask();

    chooseAnswer(wrongAnswer.word);

    await customTimeout();

    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-isnt-correct');

    await settled();

    chooseAnswer(targetTask.correctAnswer.word);

    await customTimeout();

    assert.dom('[data-test-right-answer-notification]').exists();
  });

  test('goest to next task after a right answer picture', async function(assert) {
    await pageObject.goToFirstTask();

    const { targetTask } = setupAfterPageVisit();

    await pageObject.startTask();

    chooseAnswer(targetTask.correctAnswer.word);

    await customTimeout();

    assert.dom('[data-test-right-answer-notification]').exists();

    await waitFor('[data-test-task-id="2"]');
    assert.dom('[data-test-task-id="2"]').exists();
  });

  test('sends a POST request to "study-history" after exercise completed', async function(assert) {
    assert.expect(1);

    /* eslint-disable no-undef */
    server.post('/study-history', function(request) {
      assert.ok(true, 'sends a post request');
      return { id: 1, ...JSON.parse(request.requestBody) };
    });

    await pageObject.goToFirstTaskSecondExercise();

    let { targetTask } = setupAfterPageVisit();

    await pageObject.startTask();
    await chooseAnswer(targetTask.correctAnswer.word);

    await customTimeout();

    await click('[data-test-continue]');

    // const targetTask2 = setupAfterPageVisit().targetTask;
    // await waitFor('[data-test-task-answer-option]');
    // await chooseAnswer(targetTask2.correctAnswer.word);
    await customTimeout();
  });

  test('shows a complete victory widget after exercise completed and goes to series route', async function(assert) {
    /* eslint-disable no-undef */
    server.put('exercises/1', function() {});

    await pageObject.goToFirstTask();

    let { targetTask } = setupAfterPageVisit();

    await pageObject.startTask();

    chooseAnswer(targetTask.correctAnswer.word);
    await timeout(TIMINGS.SUCCESS_ANSWER_NOTIFICATION_STARTED);

    assert.dom('[data-test-right-answer-notification]').exists();

    await waitFor('[data-test-task-id="2"]');
    assert.dom('[data-test-task-id="2"]').exists();

    const targetTask2 = setupAfterPageVisit().targetTask;

    await timeout(TIMINGS.FAKE_AUDIO_FINISHED);
    chooseAnswer(targetTask2.correctAnswer.word);
    await customTimeout();

    assert.dom('[data-test-right-answer-notification]').exists();
    await waitFor('[data-test-answer-correctness-widget]');


    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-is-correct');

    await waitFor('[data-test-exercise-stats]');

    await click('[data-test-continue]');

    await customTimeout();
    await customTimeout();


    assert.equal(currentURL(), '/groups/1/series/1/subgroup/1');
  });
});
