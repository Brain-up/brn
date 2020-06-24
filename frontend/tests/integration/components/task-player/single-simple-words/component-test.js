import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import data from './test-support/data-storage';
import AudioService from 'brn/services/audio';
import { chooseAnswer } from './test-support/helper';

module('Integration | Component | task-player/single-simple-words', function(
  hooks,
) {
  setupRenderingTest(hooks);
  let counter = 0;

  hooks.beforeEach(async function() {
    const store = this.owner.lookup('service:store');
    let model = store.createRecord('task/single-simple-words', data.task);
    this.set('model', model);

    this.set('mockTimerService', {
      isPaused: false,
      isStarted: true,
      runTimer() {},
    });
    counter = 0;
  });

  test('it renders', async function(assert) {
    await render(
      hbs`<TaskPlayer::SingleSimpleWords @task={{this.model}} @mode="task" />`,
    );

    assert.dom('[data-test-task-answer-option="вить"]').exists();
  });

  test('the "startPlayTask" function should not be called if a task is the last and the answer is correct', async function(assert) {
    this.set('onRightAnswer', function() {
      assert.ok(true, 'calls onRightAnswer');
    });

    this.set('onWrongAnswer', function() {
      assert.ok(true, 'calls onWrongAnswer');
    });

    class MockAudio extends AudioService {
      startPlayTask() {
        counter++;
      }
    }

    this.owner.register('service:audio', MockAudio);

    await render(hbs`
      <TaskPlayer::SingleSimpleWords @onWrongAnswer={{this.onWrongAnswer}} @onRightAnswer={{this.onRightAnswer}} @task={{this.model}} @mode="task" @studyingTimer={{this.mockTimerService}} />
    `);

    await chooseAnswer(this.model.correctAnswer);

    assert.equal(counter, 2);
  });
});
