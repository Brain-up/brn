import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import AudioService from 'brn/services/audio';

function makeAnswerOption(word, signalId, duration, frequency) {
  return {
    word,
    signalId,
    signal: { duration: duration || 500, frequency: frequency || 200 },
  };
}

module('Integration | Component | task-player/signal', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  hooks.beforeEach(function () {
    class MockAudio extends AudioService {
      startPlayTask() { /* noop */ }
    }
    this.owner.register('service:audio', MockAudio);

    this.set('noop', function () { /* noop */ });
  });

  test('it renders with answer options', async function (assert) {
    const store = this.owner.lookup('service:store');
    const exercise = store.createRecord('exercise');
    const task = store.createRecord('task/signal', {
      exercise,
      answerOptions: [
        makeAnswerOption('1: [500ms, 200Mhz]', '1', 500, 200),
        makeAnswerOption('2: [300ms, 400Mhz]', '2', 300, 400),
      ],
    });
    this.set('task', task);

    await render(hbs`
      <TaskPlayer::Signal
        @task={{this.task}}
        @mode="task"
        @onPlayText={{this.noop}}
        @onRightAnswer={{this.noop}}
        @onWrongAnswer={{this.noop}}
      />
    `);

    assert.dom('[data-test-task-answer]').exists(
      { count: 2 },
      'renders 2 answer buttons for 2 answer options',
    );
  });

  test('answer buttons are disabled when @disableAnswers is true', async function (assert) {
    const store = this.owner.lookup('service:store');
    const exercise = store.createRecord('exercise');
    const task = store.createRecord('task/signal', {
      exercise,
      answerOptions: [
        makeAnswerOption('1: [500ms, 200Mhz]', '1', 500, 200),
      ],
    });
    this.set('task', task);

    await render(hbs`
      <TaskPlayer::Signal
        @task={{this.task}}
        @mode="task"
        @disableAnswers={{true}}
        @onPlayText={{this.noop}}
        @onRightAnswer={{this.noop}}
        @onWrongAnswer={{this.noop}}
      />
    `);

    assert.dom('[data-test-task-answer]').hasAttribute(
      'disabled',
      '',
      'answer button is disabled',
    );
  });

  test('answer buttons show the word label', async function (assert) {
    const store = this.owner.lookup('service:store');
    const exercise = store.createRecord('exercise');
    const task = store.createRecord('task/signal', {
      exercise,
      answerOptions: [
        makeAnswerOption('Signal A', '1', 500, 200),
      ],
    });
    this.set('task', task);

    await render(hbs`
      <TaskPlayer::Signal
        @task={{this.task}}
        @mode="task"
        @onPlayText={{this.noop}}
        @onRightAnswer={{this.noop}}
        @onWrongAnswer={{this.noop}}
      />
    `);

    assert.dom('[data-test-task-answer-option="Signal A"]').exists(
      'renders button with correct data-test attribute for word',
    );
  });
});
