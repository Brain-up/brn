/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
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
      const self = this;




      await render(
        <template><TaskPlayer::SingleSimpleWords @task={{self.model}} @mode="task" /></template>
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

      const self = this;




      await render(
        <template><TaskPlayer::SingleSimpleWords @onWrongAnswer={{self.onWrongAnswer}} @onRightAnswer={{self.onRightAnswer}} @task={{self.model}} @mode="task" @studyingTimer={{self.mockTimerService}} /></template>
      );

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
  'Integration | Component | task-player/single-simple-words | column layout',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');

    test('data-cols matches option count when fewer options than wordsColumns', async function (assert) {
      const store = this.owner.lookup('service:store');
      const model = store.createRecord('task/single-simple-words', {
        ...schemaData(task),
        answerOptions: [
          { id: 1, word: 'кот', columnNumber: -1 },
          { id: 2, word: 'пёс', columnNumber: -1 },
        ],
        exercise: store.createRecord('exercise', { wordsColumns: 3 }),
      });
      this.set('model', model);

      const self = this;




      await render(
        <template><TaskPlayer::SingleSimpleWords @task={{self.model}} @mode="task" /></template>
      );

      const option = this.element.querySelector('[data-cols]');
      assert.strictEqual(
        option.getAttribute('data-cols'),
        '2',
        'data-cols is 2 (min of wordsColumns=3 and 2 options)',
      );
    });

    test('data-cols uses wordsColumns when options count is equal or greater', async function (assert) {
      const store = this.owner.lookup('service:store');
      const model = store.createRecord('task/single-simple-words', {
        ...schemaData(task),
        answerOptions: [
          { id: 1, word: 'a', columnNumber: -1 },
          { id: 2, word: 'b', columnNumber: -1 },
          { id: 3, word: 'c', columnNumber: -1 },
        ],
        exercise: store.createRecord('exercise', { wordsColumns: 3 }),
      });
      this.set('model', model);

      const self = this;




      await render(
        <template><TaskPlayer::SingleSimpleWords @task={{self.model}} @mode="task" /></template>
      );

      const option = this.element.querySelector('[data-cols]');
      assert.strictEqual(
        option.getAttribute('data-cols'),
        '3',
        'data-cols matches wordsColumns when enough options exist',
      );
    });

    test('sortedAnswerOptions returns flat array when all columnNumbers are -1', async function (assert) {
      const store = this.owner.lookup('service:store');
      const model = store.createRecord('task/single-simple-words', {
        ...schemaData(task),
        answerOptions: [
          { id: 1, word: 'x', columnNumber: -1 },
          { id: 2, word: 'y', columnNumber: -1 },
        ],
        exercise: store.createRecord('exercise', { wordsColumns: 5 }),
      });
      this.set('model', model);

      const self = this;




      await render(
        <template><TaskPlayer::SingleSimpleWords @task={{self.model}} @mode="task" /></template>
      );

      // Both options should render — no console.warn about column mismatch
      const options = this.element.querySelectorAll('[data-test-task-answer-option]');
      assert.strictEqual(options.length, 2, 'all options rendered');
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
          isAudioFileUrlGenerated: true,
        }),
      });
      this.set('model', model);
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});

      const self = this;




      await render(<template><TaskPlayer::SingleSimpleWords
      @onWrongAnswer={{self.onWrongAnswer}}
      @onRightAnswer={{self.onRightAnswer}}
      @task={{self.model}}
      @mode="task"
      /></template>);

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
          isAudioFileUrlGenerated: false,
        }),
      });
      this.set('model', model);
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});

      const self = this;




      await render(<template><TaskPlayer::SingleSimpleWords
      @onWrongAnswer={{self.onWrongAnswer}}
      @onRightAnswer={{self.onRightAnswer}}
      @task={{self.model}}
      @mode="task"
      /></template>);

      assert.ok(receivedUrls.length > 0, 'startPlayTask was called');
      assert.ok(
        receivedUrls[0].includes('/api/audio?text='),
        `autoplay falls back to TTS URL "${receivedUrls[0]}"`,
      );
    });
  },
);
