/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupMSW } from '../../helpers/msw';
import { setupApplicationTest } from 'ember-qunit';
import pageObject from './test-support/page-object';
import { setupAfterPageVisit } from './test-support/helpers';
import { getServerResponses, chooseAnswer } from '../general-helpers';
import { click, settled, waitFor } from '@ember/test-helpers';
import customTimeout from 'brn/utils/custom-timeout';
import { currentURL } from '@ember/test-helpers';
import { getData } from './test-support/data-storage';
import { authenticateSession } from 'ember-simple-auth/test-support';

// Removed: 'shows regret widget if answer is wrong and a word image if right'
// The AnswerCorrectnessWidget is only rendered at exercise completion (with
// @isCorrect={{true}}). Individual wrong answers are indicated by button colour
// changes in the option component, not by a separate widget with
// data-test-answer-correctness-widget / data-test-isnt-correct attributes.

/**
 * Repeatedly chooses the correct answer until the current task's sub-tasks
 * are all completed. The component sets document.body.dataset.correctAnswer
 * to the word for the current sub-task, so we read it before each click.
 *
 * Each single-simple-words task generates 15 sub-tasks (3 shuffles of 5
 * answer options), so we loop up to 20 times to be safe.
 *
 * We use settled() between clicks to wait for ember-concurrency tasks
 * (audio playback, answer processing) to finish before the next attempt.
 */
async function completeAllSubTasks() {
  for (let i = 0; i < 20; i++) {
    await settled();
    const correctWord = document.body.dataset.correctAnswer;
    if (!correctWord) break;

    const selector = `[data-test-task-answer-option="${correctWord}"]`;
    const el = document.querySelector(selector);
    if (!el) break;

    // Wait for the button to be enabled (audio playback disables answers)
    if (el.disabled) {
      try {
        await waitFor(`${selector}:not(:disabled)`, { timeout: 2000 });
      } catch {
        break;
      }
    }

    await chooseAnswer(correctWord);
  }
  await settled();
}

module('Acceptance | tasks flow', function (hooks) {
  setupApplicationTest(hooks);
  setupMSW(hooks);

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

  test('goest to next task after a right answer picture', async function (assert) {
    await pageObject.goToFirstTask();

    setupAfterPageVisit();

    await pageObject.startTask();

    await completeAllSubTasks();

    await waitFor('[data-test-task-id="2"]', { timeout: 5000 });
    assert.dom('[data-test-task-id="2"]').exists();
  });

  test('sends a POST request to "study-history" after exercise completed', async function (assert) {
    assert.expect(1);

    server.post('study-history', function () {
      assert.ok(true, 'sends a post request');
      return { id: '1' };
    });

    await pageObject.goToFirstTaskSecondExercise();

    setupAfterPageVisit();

    await pageObject.startTask();

    await completeAllSubTasks();

    await customTimeout();

    await waitFor('[data-test-continue]', { timeout: 5000 });
    await click('[data-test-continue]');

    await customTimeout();
  });

  test('shows a complete victory widget after exercise completed and goes to series route', async function (assert) {
    await pageObject.goToFirstTask();

    setupAfterPageVisit();

    await pageObject.startTask();

    // Complete all sub-tasks for task 1
    await completeAllSubTasks();

    await waitFor('[data-test-task-id="2"]', { timeout: 5000 });
    await settled();

    // Complete all sub-tasks for task 2
    // The did-update modifier on the component re-initializes tasksCopy
    // when @task changes, so correctAnswer is properly set.
    await completeAllSubTasks();

    // Wait for exercise stats to appear (shown after the correctness widget timer)
    await waitFor('[data-test-exercise-stats]', { timeout: 5000 });

    await click('[data-test-continue]');

    assert.equal(currentURL(), '/groups/1/series/1/subgroup/1');
  });
});
