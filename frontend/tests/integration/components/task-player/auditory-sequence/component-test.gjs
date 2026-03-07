/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import AudioService from 'brn/services/audio';
import StatsService, { StatEvents } from 'brn/services/stats';
import AuditorySequence from 'brn/components/task-player/auditory-sequence';

// Strip fields that SchemaRecord rejects (reserved 'type', non-schema 'wrongAnswers')
function schemaData(obj) {
  const copy = Object.assign({}, obj);
  delete copy.type;
  delete copy.wrongAnswers;
  return copy;
}

const task = {
  exerciseMechanism: 'WORDS',
  exerciseType: 'AUDITORY_SEQUENCE',
  type: 'task/auditory-sequence',
  name: '',
  level: 0,
  shouldBeWithPictures: true,
  wrongAnswers: [],
  correctAnswer: 'кот',
  answerOptions: [
    {
      id: 301,
      audioFileUrl: '',
      word: 'кот',
      wordType: 'OBJECT',
      pictureFileUrl: 'pictures/кот.jpg',
      soundsCount: 0,
      description: '',
      columnNumber: -1,
    },
    {
      id: 302,
      audioFileUrl: '',
      word: 'дом',
      wordType: 'OBJECT',
      pictureFileUrl: 'pictures/дом.jpg',
      soundsCount: 0,
      description: '',
      columnNumber: -1,
    },
  ],
};

module(
  'Integration | Component | task-player/auditory-sequence',
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
      let model = store.createRecord('task/auditory-sequence', {
        ...schemaData(task),
        exercise: store.createRecord('exercise', { playWordsCount: 2 }),
      });
      this.set('model', model);
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});
    });

    test('it renders answer options', async function (assert) {
      const self = this;

      await render(
        <template><AuditorySequence @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      assert.dom('[data-test-task-answer-option="кот"]').exists();
      assert.dom('[data-test-task-answer-option="дом"]').exists();
    });

    test('handles correct answer sequence and records RightAnswer stat', async function (assert) {
      const self = this;

      await render(
        <template><AuditorySequence @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // The correct sequence is stored as comma-separated words
      const correctSequence = document.body.dataset.correctAnswer.split(',');
      for (const word of correctSequence) {
        await click(`[data-test-task-answer-option="${word}"]`);
      }

      assert.true(
        recordedEvents.includes(StatEvents.RightAnswer),
        'RightAnswer stat was recorded',
      );
    });

    test('handles wrong answer sequence and records WrongAnswer stat', async function (assert) {
      const self = this;

      await render(
        <template><AuditorySequence @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // Click words in reverse order to produce a wrong sequence
      const correctSequence = document.body.dataset.correctAnswer.split(',');
      const wrongSequence = [...correctSequence].reverse();
      for (const word of wrongSequence) {
        await click(`[data-test-task-answer-option="${word}"]`);
      }

      assert.true(
        recordedEvents.includes(StatEvents.WrongAnswer),
        'WrongAnswer stat was recorded',
      );
    });
  },
);
