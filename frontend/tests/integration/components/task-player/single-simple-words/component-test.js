import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import data from './test-support/data-storage';
import AudioService from 'brn/services/audio';

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
  });

  test('it rende0rs', async function(assert) {
    await render(
      hbs`<TaskPlayer::SingleSimpleWords @task={{this.model}} @mode="task" />`,
    );

    assert.equal(this.element.textContent.trim(), '');
  });

  test('the "startPlayTask" function should not be called if a task is the last and the answer is correct', async function(assert) {
    this.set('onRightAnswer', function() {
      assert.ok(true, 'calls onRightAnswer');
    });

    class MockAudio extends AudioService {
      startPlayTask() {
        counter++;
      }
    }

    this.owner.register('service:audio', MockAudio);

    await render(hbs`
      <TaskPlayer::SingleSimpleWords @onRightAnswer={{this.onRightAnswer}} @task={{this.model}} @mode="task" @studyingTimer={{this.mockTimerService}}/>
    `);
    // await pageObject.startTask();
    assert.equal(counter, 1);
    counter = 0;
  });
});
