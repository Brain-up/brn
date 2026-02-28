/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import { task, taskWithPreGeneratedAudio } from './test-support/data-storage';
import AudioService from 'brn/services/audio';
import { chooseAnswer } from './test-support/helper';

// Strip fields that SchemaRecord rejects (reserved 'type', non-schema 'wrongAnswers')
function schemaData(obj) {
  const copy = Object.assign({}, obj);
  delete copy.type;
  delete copy.wrongAnswers;
  return copy;
}

module(
  'Integration | Component | task-player/single-simple-words',
  function (hooks) {
    setupRenderingTest(hooks);setupIntl(hooks, 'en-us');
    let counter = 0;

    hooks.beforeEach(async function () {
      const store = this.owner.lookup('service:store');
      let model = store.createRecord('task/single-simple-words', {
        ...schemaData(task),
        exercise: store.createRecord('exercise')
      });
      this.set('model', model);

      this.set('mockTimerService', {
        isPaused: false,
        isStarted: true,
        runTimer() {},
      });
      counter = 0;
    });

    test('it renders', async function (assert) {
      await render(
        hbs`<TaskPlayer::SingleSimpleWords @task={{this.model}} @mode="task" />`,
      );

      assert.dom('[data-test-task-answer-option="вить"]').exists();
    });

    test('the "startPlayTask" function should not be called if a task is the last and the answer is correct', async function (assert) {
      this.set('onRightAnswer', function () {
        assert.ok(true, 'calls onRightAnswer');
      });

      this.set('onWrongAnswer', function () {
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

      assert.equal(counter, 1);

      await chooseAnswer(this.model.correctAnswer);

      assert.equal(counter, 2);

      await chooseAnswer(this.model.correctAnswer);

      assert.equal(counter, 3);

      await chooseAnswer(this.model.correctAnswer);

      assert.equal(counter, 3);
    });
  },
);

module(
  'Integration | Component | task-player/single-simple-words | audio source unification',
  function (hooks) {
    setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

    test('startPlayTask receives the pre-generated audio URL when available', async function (assert) {
      const receivedUrls = [];

      class MockAudio extends AudioService {
        startPlayTask(files) {
          receivedUrls.push(...(files || []));
        }
      }

      this.owner.register('service:audio', MockAudio);

      const store = this.owner.lookup('service:store');
      let model = store.createRecord('task/single-simple-words', {
        ...schemaData(taskWithPreGeneratedAudio),
        exercise: store.createRecord('exercise', {
          audioFileUrlGenerated: true,
        }),
      });
      this.set('model', model);
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});

      await render(hbs`
        <TaskPlayer::SingleSimpleWords
          @onWrongAnswer={{this.onWrongAnswer}}
          @onRightAnswer={{this.onRightAnswer}}
          @task={{this.model}}
          @mode="task"
        />
      `);

      assert.ok(receivedUrls.length > 0, 'startPlayTask was called');
      assert.ok(
        receivedUrls[0].includes('/audio/no_noise/'),
        `autoplay uses pre-generated URL "${receivedUrls[0]}" instead of /api/audio?text=`,
      );
      assert.notOk(
        receivedUrls[0].includes('/api/audio?text='),
        'autoplay does not use the TTS API URL',
      );
    });

    test('startPlayTask falls back to TTS URL when no pre-generated audio', async function (assert) {
      const receivedUrls = [];

      class MockAudio extends AudioService {
        startPlayTask(files) {
          receivedUrls.push(...(files || []));
        }
      }

      this.owner.register('service:audio', MockAudio);

      const store = this.owner.lookup('service:store');
      let model = store.createRecord('task/single-simple-words', {
        ...schemaData(task),
        exercise: store.createRecord('exercise', {
          audioFileUrlGenerated: false,
        }),
      });
      this.set('model', model);
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});

      await render(hbs`
        <TaskPlayer::SingleSimpleWords
          @onWrongAnswer={{this.onWrongAnswer}}
          @onRightAnswer={{this.onRightAnswer}}
          @task={{this.model}}
          @mode="task"
        />
      `);

      assert.ok(receivedUrls.length > 0, 'startPlayTask was called');
      assert.ok(
        receivedUrls[0].includes('/api/audio?text='),
        `autoplay falls back to TTS URL "${receivedUrls[0]}"`,
      );
    });
  },
);
