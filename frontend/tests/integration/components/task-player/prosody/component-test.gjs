/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import AudioService from 'brn/services/audio';
import StatsService, { StatEvents } from 'brn/services/stats';
import Prosody from 'brn/components/task-player/prosody';

// Strip fields that SchemaRecord rejects (reserved 'type', non-schema 'wrongAnswers')
function schemaData(obj) {
  const copy = Object.assign({}, obj);
  delete copy.type;
  delete copy.wrongAnswers;
  return copy;
}

const task = {
  exerciseMechanism: 'WORDS',
  exerciseType: 'PROSODY',
  type: 'task/prosody',
  name: '',
  level: 0,
  shouldBeWithPictures: false,
  wrongAnswers: [],
  correctAnswer: 'Мама мыла раму.',
  prosodyType: 'STATEMENT',
  answerOptions: [
    {
      id: 401,
      audioFileUrl: '',
      word: 'Мама мыла раму.',
      wordType: 'OBJECT',
      pictureFileUrl: '',
      soundsCount: 0,
      description: '',
      columnNumber: -1,
    },
    {
      id: 402,
      audioFileUrl: '',
      word: 'Мама мыла раму?',
      wordType: 'OBJECT',
      pictureFileUrl: '',
      soundsCount: 0,
      description: '',
      columnNumber: -1,
    },
  ],
};

module(
  'Integration | Component | task-player/prosody',
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
      let model = store.createRecord('task/prosody', {
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
        <template><Prosody @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      assert.dom('[data-test-task-answer-option="Мама мыла раму."]').exists();
      assert.dom('[data-test-task-answer-option="Мама мыла раму?"]').exists();
    });

    test('handles correct answer and records RightAnswer stat', async function (assert) {
      const self = this;

      await render(
        <template><Prosody @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
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
        <template><Prosody @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
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
