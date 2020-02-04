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

    assert.ok(pageObject.startButtonIsPresent, 'start button is present');

    await pageObject.startTask();

    assert.notOk(
      pageObject.startButtonIsPresent,
      'start button is not present',
    );
  });

  test('shows regret widget if answer is wrong and a word image if right', async function(assert) {
    await pageObject.goToFirstTask();

    await pageObject.startTask();

    pageObject.wrongAnswers[0].choose();

    await customTimeout();

    assert.equal(
      pageObject.correctnessWidgetIndicatesIncorrect,
      '',
      'wrong answer notification',
    );

    await settled();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.ok(
      pageObject.rightAnswerNotificationExists,
      'right answer notification is present',
    );
  });

  test('goest to next task after a right answer picture', async function(assert) {
    await pageObject.goToFirstTask();

    await pageObject.startTask();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.ok(
      pageObject.rightAnswerNotificationExists,
      'right answer notification is present',
    );

    await customTimeout();

    assert.ok(pageObject.secondTaskIsShown, 'moved to the next task');
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

    assert.ok(
      pageObject.rightAnswerNotificationExists,
      'right answer notification is present',
    );

    await customTimeout();
    await customTimeout();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.ok(
      pageObject.rightAnswerNotificationExists,
      'right answer notification is present',
    );

    await customTimeout();

    assert.equal(
      pageObject.correctnessWidgetIndicatesCorrect,
      '',
      'correct answer notification',
    );
  });

  test('shows a complete victory widget after exercise completed and goes to series route', async function(assert) {
    /* eslint-disable no-undef */
    server.put('exercises/1', function() {});

    await pageObject.goToFirstTask();

    await pageObject.startTask();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.ok(
      pageObject.rightAnswerNotificationExists,
      'right answer notification is present',
    );

    await customTimeout();

    assert.ok(pageObject.secondTaskIsShown, 'moved to the next task');

    await customTimeout();

    pageObject.chooseRightAnswer();

    await customTimeout();

    assert.ok(
      pageObject.rightAnswerNotificationExists,
      'right answer notification is present',
    );

    await customTimeout();

    assert.equal(
      pageObject.correctnessWidgetIndicatesCorrect,
      '',
      'correct answer notification',
    );

    await customTimeout();
    await customTimeout();

    assert.equal(currentURL(), '/groups/1/series/1');
  });
});
