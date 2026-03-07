/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import AudioService from 'brn/services/audio';
import StatsService, { StatEvents } from 'brn/services/stats';
import PhonemePairs from 'brn/components/task-player/phoneme-pairs';

// Strip fields that SchemaRecord rejects (reserved 'type', non-schema 'wrongAnswers')
function schemaData(obj) {
  const copy = Object.assign({}, obj);
  delete copy.type;
  delete copy.wrongAnswers;
  return copy;
}

const task = {
  exerciseMechanism: 'WORDS',
  exerciseType: 'PHONEME_PAIRS',
  type: 'task/phoneme-pairs',
  name: '',
  level: 0,
  shouldBeWithPictures: true,
  wrongAnswers: [],
  correctAnswer: 'бак',
  contrastType: 'VOICING',
  answerOptions: [
    {
      id: 201,
      audioFileUrl: '',
      word: 'бак',
      wordType: 'OBJECT',
      pictureFileUrl: 'pictures/бак.jpg',
      soundsCount: 0,
      description: '',
      columnNumber: -1,
    },
    {
      id: 202,
      audioFileUrl: '',
      word: 'пак',
      wordType: 'OBJECT',
      pictureFileUrl: 'pictures/пак.jpg',
      soundsCount: 0,
      description: '',
      columnNumber: -1,
    },
  ],
};

module(
  'Integration | Component | task-player/phoneme-pairs',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');
    let recordedEvents;

    hooks.beforeEach(async function () {
      recordedEvents = [];

      class MockAudio extends AudioService {
        startPlayTask() {}
        stop() {}
        get isBusy() { return false; }
      }

      class MockStats extends StatsService {
        addEvent(eventName) {
          recordedEvents.push(eventName);
        }
      }

      this.owner.register('service:audio', MockAudio);
      this.owner.register('service:stats', MockStats);

      const store = this.owner.lookup('service:store');
      let model = store.createRecord('task/phoneme-pairs', {
        ...schemaData(task),
        exercise: store.createRecord('exercise'),
      });
      this.set('model', model);
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});
    });

    test('it renders answer options', async function (assert) {
      const self = this;

      await render(
        <template><PhonemePairs @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      assert.dom('[data-test-task-answer-option="бак"]').exists();
      assert.dom('[data-test-task-answer-option="пак"]').exists();
    });

    test('handles correct answer and records RightAnswer stat', async function (assert) {
      const self = this;

      await render(
        <template><PhonemePairs @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;

      await click(`[data-test-task-answer-option="${correctAnswer}"]`);

      assert.true(
        recordedEvents.includes(StatEvents.RightAnswer),
        'RightAnswer stat was recorded',
      );
    });

    test('handles wrong answer and records WrongAnswer stat', async function (assert) {
      const self = this;

      await render(
        <template><PhonemePairs @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;
      const wrongOption = task.answerOptions.find((o) => o.word !== correctAnswer);

      await click(`[data-test-task-answer-option="${wrongOption.word}"]`);

      assert.true(
        recordedEvents.includes(StatEvents.WrongAnswer),
        'WrongAnswer stat was recorded',
      );
    });
  },
);
