/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click, settled } from '@ember/test-helpers';
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

// Wrap a store record with custom tasksToSolve that uses a fixed sequence
// so the sequence order is deterministic regardless of shuffleArray randomness.
function wrapModelWithFixedSequence(model, sequence) {
  const proxy = new Proxy(model, {
    get(target, prop) {
      if (prop === 'tasksToSolve') {
        return [
          { answer: sequence, order: 0 },
          { answer: [...sequence].reverse(), order: 1 },
        ];
      }
      const value = Reflect.get(target, prop);
      if (typeof value === 'function') {
        return value.bind(target);
      }
      return value;
    },
  });
  return proxy;
}

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

      // Build the fixed sequence from the raw answer options
      const fixedSequence = task.answerOptions.map((o) => ({
        ...o,
        audioFileUrl: o.audioFileUrl || null,
        pictureFileUrl: o.pictureFileUrl || null,
      }));
      this.set('wrappedModel', wrapModelWithFixedSequence(model, fixedSequence));
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
        <template><AuditorySequence @task={{self.wrappedModel}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // The fixed sequence for the first task is [кот, дом]
      const correctSequence = task.answerOptions.map((o) => o.word);

      // Click options in the correct order to complete the sequence
      for (const word of correctSequence) {
        await click(`[data-test-task-answer-option="${word}"]`);
      }
      await settled();

      assert.true(
        recordedEvents.includes(StatEvents.RightAnswer),
        'RightAnswer stat was recorded',
      );
    });

    test('handles wrong answer sequence and records WrongAnswer stat', async function (assert) {
      const self = this;

      await render(
        <template><AuditorySequence @task={{self.wrappedModel}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // The fixed sequence for the first task is [кот, дом]
      const correctSequence = task.answerOptions.map((o) => o.word);

      // Click options in reversed order to produce a wrong answer
      const wrongSequence = [...correctSequence].reverse();
      for (const word of wrongSequence) {
        await click(`[data-test-task-answer-option="${word}"]`);
      }
      await settled();

      assert.true(
        recordedEvents.includes(StatEvents.WrongAnswer),
        'WrongAnswer stat was recorded',
      );
    });
  },
);
