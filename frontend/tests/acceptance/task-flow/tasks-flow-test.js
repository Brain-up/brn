import { module, test } from 'qunit';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';
import { setupApplicationTest } from 'ember-qunit';
import pageObject from './test-support/page-object';
import { settled } from '@ember/test-helpers';
import customTimeout from 'brn/utils/custom-timeout';
import { currentURL } from '@ember/test-helpers';
import taskFlowScenario from '../../../mirage/scenarios/task-flow';

module('Acceptance | tasks flow', function(hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  hooks.beforeEach(() => {
    /* eslint-disable no-undef */
    taskFlowScenario(server);
  });

  test('has a start task button if the task is not started yet', async function(assert) {
    await pageObject.goToFirstTask();

    assert.dom('[data-test-start-task-button]').exists();

    await pageObject.startTask();

    assert.dom('[data-test-start-task-button]').doesNotExist();
  });

  test('shows regret widget if answer is wrong and a word image if right', async function(assert) {
    await pageObject.goToFirstTask();

    await pageObject.startTask();

    pageObject.wrongAnswers[0].choose();

    await customTimeout();

    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-isnt-correct');

    await settled();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.dom('[data-test-right-answer-notification]').exists();
  });

  test('goest to next task after a right answer picture', async function(assert) {
    await pageObject.goToFirstTask();

    await pageObject.startTask();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.dom('[data-test-right-answer-notification]').exists();

    await customTimeout();

    assert.dom('[data-test-task-id="2"]').exists();
  });

  test('sends a POST request to "study-history" after exercise completed', async function(assert) {
    assert.expect(4);

    /* eslint-disable no-undef */
    server.post('/study-history', function(request) {
      assert.ok(true, 'sends a post request');
      return { id: 1, ...JSON.parse(request.requestBody) };
    });

    await pageObject.goToFirstTaskSecondExercise();

    await pageObject.startTask();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.dom('[data-test-right-answer-notification]').exists();

    await customTimeout();
    await customTimeout();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.dom('[data-test-right-answer-notification]').exists();

    await customTimeout();

    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-is-correct');
  });

  test('shows a complete victory widget after exercise completed and goes to series route', async function(assert) {
    /* eslint-disable no-undef */
    server.put('exercises/1', function() {});

    await pageObject.goToFirstTask();

    await pageObject.startTask();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.dom('[data-test-right-answer-notification]').exists();

    await customTimeout();

    assert.dom('[data-test-task-id="2"]').exists();

    await customTimeout();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.dom('[data-test-right-answer-notification]').exists();

    await customTimeout();

    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-is-correct');

    await customTimeout();
    await customTimeout();

    assert.equal(currentURL(), '/groups/1/series/1');
  });
});
