import { module, test } from 'qunit';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';
import { setupApplicationTest } from 'ember-qunit';
import pageObject from './test-support/page-object';
import {
  getServerResponses,
  chooseAnswer,
  setupAfterPageVisit,
} from './test-support/helpers';
import { timeout } from 'ember-concurrency';
import { settled } from '@ember/test-helpers';

module('Acceptance | tasks flow', function(hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  test('shows regret widget if answer is wrong and a word image if right', async function(assert) {
    getServerResponses();

    await pageObject.goToFirstTask();

    const { targetTask, wrongAnswer } = setupAfterPageVisit();

    chooseAnswer(wrongAnswer.word);

    await timeout(1500);

    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-isnt-correct');

    await settled();

    chooseAnswer(targetTask.correctAnswer.word);

    await timeout(1500);

    assert.dom('[data-test-right-answer-notification]').exists();
  });

  test('goest to next task after a right answer picture', async function(assert) {
    getServerResponses();

    await pageObject.goToFirstTask();

    const { targetTask } = setupAfterPageVisit();

    chooseAnswer(targetTask.correctAnswer.word);

    await timeout(1500);

    assert.dom('[data-test-right-answer-notification]').exists();

    await timeout(2500);

    assert.dom('[data-test-task-id="2"]').exists();
  });

  test('sends a PUT request after exercise completed', async function(assert) {
    assert.expect(3);

    getServerResponses();
    /* eslint-disable no-undef */
    server.put('exercises/2', function() {
      assert.ok(true, 'sends a put request');
      return true;
    });

    await pageObject.goToFirstTaskSecondExercise();

    let { targetTask } = setupAfterPageVisit();

    chooseAnswer(targetTask.correctAnswer.word);

    await timeout(1500);

    assert.dom('[data-test-right-answer-notification]').exists();

    await timeout(4000);

    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-is-correct');

    await timeout(4000);
  });

  test('shows a complete victory widget after exercise completed', async function(assert) {
    getServerResponses();
    /* eslint-disable no-undef */
    server.put('exercises/1', function() {});

    await pageObject.goToFirstTask();

    let { targetTask } = setupAfterPageVisit();

    chooseAnswer(targetTask.correctAnswer.word);

    await timeout(1500);

    assert.dom('[data-test-right-answer-notification]').exists();

    await timeout(2500);

    assert.dom('[data-test-task-id="2"]').exists();

    const targetTask2 = setupAfterPageVisit().targetTask;

    await timeout(2500);

    chooseAnswer(targetTask2.correctAnswer.word);

    await timeout(4000);

    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-is-correct');

    await timeout(4000);

    assert
      .dom('[data-test-task-id="3"]')
      .hasAttribute('data-test-task-exercise-id', '2');
  });
});
