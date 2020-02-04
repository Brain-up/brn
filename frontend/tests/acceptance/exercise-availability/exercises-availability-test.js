import { module, test } from 'qunit';
import { setupApplicationTest } from 'ember-qunit';
import pageObject from './test-support/page-object';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';
import exerciseAvailabilityScenario from '../../../mirage/scenarios/exercise-availability';

module('Acceptance | exercises availability', function(hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  hooks.beforeEach(function() {
    /* eslint-disable no-undef */
    exerciseAvailabilityScenario(server);
  });

  test('shows the right number of exercise groups', async function(assert) {
    await pageObject.goToSeriesPage();

    assert.equal(pageObject.exerciseGroupsCount, 2, 'has two groups (by name)');
    assert.equal(pageObject.exercisesCount, 4, 'has four exercises');
  });

  test('first exercices in the name group is available by default', async function(assert) {
    await pageObject.goToSeriesPage();

    assert.equal(
      pageObject.firstLevelExercisesCount,
      2,
      'has two first level exercises',
    );

    assert.notOk(
      pageObject.firstGroupFirstExerciseIsDisabled,
      'group 1: first exercise is not disabled',
    );
    assert.notOk(
      pageObject.secondGroupFirstExerciseIsDisabled,
      'group 2: first exercise is not disabled',
    );
    assert.ok(
      pageObject.firstGroupSecondExerciseIsDisabled,
      'group 1: second exercise is disabled',
    );
    assert.ok(
      pageObject.secondGroupSecondExerciseIsDisabled,
      'group 2: second exercise is disabled',
    );
  });

  test('marks available exercises withing a name group if previous is completed', async function(assert) {
    await pageObject.goToSeriesPage();

    await pageObject.goToFirstExercisePage();
    await pageObject.startTask();

    await pageObject.chooseRightAnswer();
    assert.notOk(
      pageObject.firstGroupFirstExerciseIsDisabled,
      'group 1: first exercise is not disabled',
    );
    assert.notOk(
      pageObject.firstGroupSecondExerciseIsDisabled,
      'group 1: second exercise is disabled',
    );
  });
});
