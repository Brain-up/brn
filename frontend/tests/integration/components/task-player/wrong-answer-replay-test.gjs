/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import AudioService from 'brn/services/audio';
import StatsService from 'brn/services/stats';
import PhonemePairs from 'brn/components/task-player/phoneme-pairs';
import Prosody from 'brn/components/task-player/prosody';
import AuditorySequence from 'brn/components/task-player/auditory-sequence';
import EnvironmentalSounds from 'brn/components/task-player/environmental-sounds';
import SingleSimpleWords from 'brn/components/task-player/single-simple-words';

// Tests for the replay-on-wrong-answer feature.
//
// When a user answers incorrectly, the same task's audio should be replayed
// automatically and the user must answer correctly before moving to the next task.
//
// These tests verify:
// 1. audio.startPlayTask is called again after a wrong answer (audio replay)
// 2. The same task stays active after a wrong answer (no skip to next)
// 3. After a correct answer following a wrong one, the task completes normally

// Strip fields that SchemaRecord rejects (reserved 'type', non-schema 'wrongAnswers')
function schemaData(obj) {
  const copy = Object.assign({}, obj);
  delete copy.type;
  delete copy.wrongAnswers;
  return copy;
}

// ---------------------------------------------------------------------------
// 1. PhonemePairsComponent - audio replays on wrong answer
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-replay | phoneme-pairs',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');

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
          id: 201, audioFileUrl: '', word: 'бак', wordType: 'OBJECT',
          pictureFileUrl: '', soundsCount: 0, description: '', columnNumber: -1,
        },
        {
          id: 202, audioFileUrl: '', word: 'пак', wordType: 'OBJECT',
          pictureFileUrl: '', soundsCount: 0, description: '', columnNumber: -1,
        },
      ],
    };

    hooks.beforeEach(async function () {
      this.startPlayTaskCalls = 0;
      this.stopCalls = 0;
      const testContext = this;

      class MockAudio extends AudioService {
        startPlayTask() { testContext.startPlayTaskCalls++; }
        async stop() { testContext.stopCalls++; }
        get isBusy() { return false; }
      }
      class MockStats extends StatsService {
        addEvent() {}
      }
      this.owner.register('service:audio', MockAudio);
      this.owner.register('service:stats', MockStats);

      const store = this.owner.lookup('service:store');
      this.set('model', store.createRecord('task/phoneme-pairs', {
        ...schemaData(task),
        exercise: store.createRecord('exercise'),
      }));
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});
    });

    test('audio is replayed after wrong answer', async function (assert) {
      const self = this;
      await render(
        <template><PhonemePairs @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const initialCalls = this.startPlayTaskCalls;

      // Click wrong answer
      await click('[data-test-task-answer-option="пак"]');


      assert.true(
        this.startPlayTaskCalls > initialCalls,
        `startPlayTask was called again after wrong answer (calls: ${initialCalls} -> ${this.startPlayTaskCalls})`,
      );
    });

    test('audio.stop is called before replay to clear isBusy guard', async function (assert) {
      const self = this;
      await render(
        <template><PhonemePairs @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const initialStopCalls = this.stopCalls;

      await click('[data-test-task-answer-option="пак"]');


      assert.true(
        this.stopCalls > initialStopCalls,
        `audio.stop was called before replay (stop calls: ${initialStopCalls} -> ${this.stopCalls})`,
      );
    });

    test('same task stays after wrong answer, then correct answer completes it', async function (assert) {
      let rightAnswerCalled = false;
      this.set('onRightAnswer', function () { rightAnswerCalled = true; });
      const self = this;
      await render(
        <template><PhonemePairs @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // Wrong answer - task should stay
      await click('[data-test-task-answer-option="пак"]');


      assert.dom('[data-test-task-answer-option="бак"]').exists(
        'correct answer option still visible after wrong answer (task not skipped)',
      );
      assert.dom('[data-test-task-answer-option="пак"]').exists(
        'wrong answer option still visible after wrong answer (task not skipped)',
      );

      // Now give correct answer
      await click('[data-test-task-answer-option="бак"]');


      assert.true(rightAnswerCalled, 'onRightAnswer was called after correct answer');
    });
  },
);

// ---------------------------------------------------------------------------
// 2. ProsodyComponent - audio replays on wrong answer
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-replay | prosody',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');

    const task = {
      exerciseMechanism: 'WORDS',
      exerciseType: 'PROSODY',
      type: 'task/prosody',
      name: '',
      level: 0,
      shouldBeWithPictures: false,
      wrongAnswers: [],
      correctAnswer: 'Statement.',
      prosodyType: 'STATEMENT',
      answerOptions: [
        {
          id: 401, audioFileUrl: '', word: 'Statement.', wordType: 'OBJECT',
          pictureFileUrl: '', soundsCount: 0, description: '', columnNumber: -1,
        },
        {
          id: 402, audioFileUrl: '', word: 'Question?', wordType: 'OBJECT',
          pictureFileUrl: '', soundsCount: 0, description: '', columnNumber: -1,
        },
      ],
    };

    hooks.beforeEach(async function () {
      this.startPlayTaskCalls = 0;
      this.stopCalls = 0;
      const testContext = this;

      class MockAudio extends AudioService {
        startPlayTask() { testContext.startPlayTaskCalls++; }
        async stop() { testContext.stopCalls++; }
        get isBusy() { return false; }
      }
      class MockStats extends StatsService {
        addEvent() {}
      }
      this.owner.register('service:audio', MockAudio);
      this.owner.register('service:stats', MockStats);

      const store = this.owner.lookup('service:store');
      this.set('model', store.createRecord('task/prosody', {
        ...schemaData(task),
        exercise: store.createRecord('exercise'),
      }));
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});
    });

    test('audio is replayed after wrong answer', async function (assert) {
      const self = this;
      await render(
        <template><Prosody @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const initialCalls = this.startPlayTaskCalls;

      await click('[data-test-task-answer-option="Question?"]');


      assert.true(
        this.startPlayTaskCalls > initialCalls,
        `startPlayTask was called again after wrong answer (calls: ${initialCalls} -> ${this.startPlayTaskCalls})`,
      );
    });

    test('audio.stop is called before replay to clear isBusy guard', async function (assert) {
      const self = this;
      await render(
        <template><Prosody @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const initialStopCalls = this.stopCalls;

      await click('[data-test-task-answer-option="Question?"]');


      assert.true(
        this.stopCalls > initialStopCalls,
        `audio.stop was called before replay (stop calls: ${initialStopCalls} -> ${this.stopCalls})`,
      );
    });

    test('same task stays after wrong answer, then correct answer completes it', async function (assert) {
      let rightAnswerCalled = false;
      this.set('onRightAnswer', function () { rightAnswerCalled = true; });
      const self = this;
      await render(
        <template><Prosody @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // Wrong answer
      await click('[data-test-task-answer-option="Question?"]');


      assert.dom('[data-test-task-answer-option="Statement."]').exists(
        'correct answer option still visible after wrong answer',
      );

      // Correct answer
      await click('[data-test-task-answer-option="Statement."]');


      assert.true(rightAnswerCalled, 'onRightAnswer was called after correct answer');
    });
  },
);

// ---------------------------------------------------------------------------
// 3. EnvironmentalSoundsComponent - audio replays on wrong answer
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-replay | environmental-sounds',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');

    const task = {
      exerciseMechanism: 'WORDS',
      exerciseType: 'ENVIRONMENTAL_SOUNDS',
      type: 'task/environmental-sounds',
      name: '',
      level: 0,
      shouldBeWithPictures: true,
      wrongAnswers: [],
      correctAnswer: 'rain',
      answerOptions: [
        {
          id: 101, audioFileUrl: '', word: 'rain', wordType: 'OBJECT',
          pictureFileUrl: '', soundsCount: 0, description: '', columnNumber: -1,
        },
        {
          id: 102, audioFileUrl: '', word: 'wind', wordType: 'OBJECT',
          pictureFileUrl: '', soundsCount: 0, description: '', columnNumber: -1,
        },
      ],
    };

    hooks.beforeEach(async function () {
      this.startPlayTaskCalls = 0;
      this.stopCalls = 0;
      const testContext = this;

      class MockAudio extends AudioService {
        startPlayTask() { testContext.startPlayTaskCalls++; }
        async stop() { testContext.stopCalls++; }
        get isBusy() { return false; }
      }
      class MockStats extends StatsService {
        addEvent() {}
      }
      this.owner.register('service:audio', MockAudio);
      this.owner.register('service:stats', MockStats);

      const store = this.owner.lookup('service:store');
      this.set('model', store.createRecord('task/environmental-sounds', {
        ...schemaData(task),
        exercise: store.createRecord('exercise'),
      }));
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});
    });

    test('audio is replayed after wrong answer', async function (assert) {
      const self = this;
      await render(
        <template><EnvironmentalSounds @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;
      const wrongWord = task.answerOptions.find(o => o.word !== correctAnswer)?.word
        || task.answerOptions.find(o => o.word !== 'rain')?.word;

      const initialCalls = this.startPlayTaskCalls;

      await click(`[data-test-task-answer-option="${wrongWord}"]`);


      assert.true(
        this.startPlayTaskCalls > initialCalls,
        `startPlayTask was called again after wrong answer (calls: ${initialCalls} -> ${this.startPlayTaskCalls})`,
      );
    });

    test('audio.stop is called before replay to clear isBusy guard', async function (assert) {
      const self = this;
      await render(
        <template><EnvironmentalSounds @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;
      const wrongWord = task.answerOptions.find(o => o.word !== correctAnswer)?.word
        || task.answerOptions.find(o => o.word !== 'rain')?.word;

      const initialStopCalls = this.stopCalls;

      await click(`[data-test-task-answer-option="${wrongWord}"]`);


      assert.true(
        this.stopCalls > initialStopCalls,
        `audio.stop was called before replay (stop calls: ${initialStopCalls} -> ${this.stopCalls})`,
      );
    });

    test('task is not completed after wrong answer, answer options remain', async function (assert) {
      let wrongAnswerCalled = false;
      this.set('onWrongAnswer', function () { wrongAnswerCalled = true; });
      const self = this;
      await render(
        <template><EnvironmentalSounds @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;
      const wrongWord = task.answerOptions.find(o => o.word !== correctAnswer)?.word
        || task.answerOptions.find(o => o.word !== 'rain')?.word;

      // Wrong answer - task should not complete, answer options should remain
      await click(`[data-test-task-answer-option="${wrongWord}"]`);


      assert.dom('[data-test-task-answer]').exists(
        'answer option buttons still visible after wrong answer (task not completed)',
      );
      assert.true(wrongAnswerCalled, 'onWrongAnswer callback was invoked');
      assert.notStrictEqual(
        document.body.dataset.correctAnswer,
        '',
        'correctAnswer is still set (still has sub-tasks to solve)',
      );
    });
  },
);

// ---------------------------------------------------------------------------
// 4. AuditorySequenceComponent - audio replays on wrong answer
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-replay | auditory-sequence',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');

    const task = {
      exerciseMechanism: 'WORDS',
      exerciseType: 'AUDITORY_SEQUENCE',
      type: 'task/auditory-sequence',
      name: '',
      level: 0,
      shouldBeWithPictures: true,
      wrongAnswers: [],
      correctAnswer: 'cat',
      answerOptions: [
        {
          id: 301, audioFileUrl: '', word: 'cat', wordType: 'OBJECT',
          pictureFileUrl: '', soundsCount: 0, description: '', columnNumber: -1,
        },
        {
          id: 302, audioFileUrl: '', word: 'dog', wordType: 'OBJECT',
          pictureFileUrl: '', soundsCount: 0, description: '', columnNumber: -1,
        },
      ],
    };

    hooks.beforeEach(async function () {
      this.startPlayTaskCalls = 0;
      this.stopCalls = 0;
      const testContext = this;

      class MockAudio extends AudioService {
        startPlayTask() { testContext.startPlayTaskCalls++; }
        async stop() { testContext.stopCalls++; }
        get isBusy() { return false; }
      }
      class MockStats extends StatsService {
        addEvent() {}
      }
      this.owner.register('service:audio', MockAudio);
      this.owner.register('service:stats', MockStats);

      const store = this.owner.lookup('service:store');
      this.set('model', store.createRecord('task/auditory-sequence', {
        ...schemaData(task),
        exercise: store.createRecord('exercise', { playWordsCount: 2 }),
      }));
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});
    });

    test('audio is replayed after wrong sequence', async function (assert) {
      const self = this;
      await render(
        <template><AuditorySequence @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctSequence = document.body.dataset.correctAnswer.split(',');
      const wrongSequence = [...correctSequence].reverse();

      if (wrongSequence.join(',') === correctSequence.join(',')) {
        assert.ok(true, 'skipped: cannot produce a wrong sequence from this data');
        return;
      }

      const initialCalls = this.startPlayTaskCalls;

      // Click wrong sequence
      for (const word of wrongSequence) {
        await click(`[data-test-task-answer-option="${word}"]`);
      }


      assert.true(
        this.startPlayTaskCalls > initialCalls,
        `startPlayTask was called again after wrong sequence (calls: ${initialCalls} -> ${this.startPlayTaskCalls})`,
      );
    });

    test('audio.stop is called before replay to clear isBusy guard', async function (assert) {
      const self = this;
      await render(
        <template><AuditorySequence @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctSequence = document.body.dataset.correctAnswer.split(',');
      const wrongSequence = [...correctSequence].reverse();

      if (wrongSequence.join(',') === correctSequence.join(',')) {
        assert.ok(true, 'skipped: cannot produce a wrong sequence from this data');
        return;
      }

      const initialStopCalls = this.stopCalls;

      // Click wrong sequence
      for (const word of wrongSequence) {
        await click(`[data-test-task-answer-option="${word}"]`);
      }


      assert.true(
        this.stopCalls > initialStopCalls,
        `audio.stop was called before replay (stop calls: ${initialStopCalls} -> ${this.stopCalls})`,
      );
    });

    test('task is not completed after wrong sequence, answer options remain', async function (assert) {
      let wrongAnswerCalled = false;
      this.set('onWrongAnswer', function () { wrongAnswerCalled = true; });
      const self = this;
      await render(
        <template><AuditorySequence @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctSequence = document.body.dataset.correctAnswer.split(',');
      const wrongSequence = [...correctSequence].reverse();

      if (wrongSequence.join(',') === correctSequence.join(',')) {
        assert.ok(true, 'skipped: cannot produce a wrong sequence from this data');
        return;
      }

      // Wrong sequence - task should not complete, options should remain
      for (const word of wrongSequence) {
        await click(`[data-test-task-answer-option="${word}"]`);
      }


      assert.dom('[data-test-task-answer]').exists(
        'answer option buttons still visible after wrong sequence (task not completed)',
      );
      assert.true(wrongAnswerCalled, 'onWrongAnswer callback was invoked');
      assert.notStrictEqual(
        document.body.dataset.correctAnswer,
        '',
        'correctAnswer is still set (still has sub-tasks to solve)',
      );
    });
  },
);

// ---------------------------------------------------------------------------
// 5. SingleSimpleWordsComponent - audio replays on wrong answer
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-replay | single-simple-words',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');

    const task = {
      exerciseMechanism: 'WORDS',
      exerciseType: 'SINGLE_SIMPLE_WORDS',
      type: 'task/WORDS',
      name: '',
      level: 0,
      shouldBeWithPictures: true,
      wrongAnswers: [],
      correctAnswer: 'dog',
      answerOptions: [
        {
          id: 345, audioFileUrl: '', word: 'dog', wordPronounce: 'dog',
          wordType: 'OBJECT', pictureFileUrl: '', soundsCount: 0,
          description: '', columnNumber: -1,
        },
        {
          id: 346, audioFileUrl: '', word: 'cat', wordPronounce: 'cat',
          wordType: 'OBJECT', pictureFileUrl: '', soundsCount: 0,
          description: '', columnNumber: -1,
        },
      ],
    };

    hooks.beforeEach(async function () {
      this.startPlayTaskCalls = 0;
      this.stopCalls = 0;
      const testContext = this;

      class MockAudio extends AudioService {
        startPlayTask() { testContext.startPlayTaskCalls++; }
        async stop() { testContext.stopCalls++; }
        get isBusy() { return false; }
      }
      class MockStats extends StatsService {
        addEvent() {}
      }
      this.owner.register('service:audio', MockAudio);
      this.owner.register('service:stats', MockStats);

      const store = this.owner.lookup('service:store');
      this.set('model', store.createRecord('task/single-simple-words', {
        ...schemaData(task),
        exercise: store.createRecord('exercise'),
      }));
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});
    });

    test('audio is replayed after wrong answer', async function (assert) {
      const self = this;
      await render(
        <template><SingleSimpleWords @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;
      const wrongWord = task.answerOptions.find(o => o.word !== correctAnswer)?.word || 'cat';

      const initialCalls = this.startPlayTaskCalls;

      await click(`[data-test-task-answer-option="${wrongWord}"]`);


      assert.true(
        this.startPlayTaskCalls > initialCalls,
        `startPlayTask was called again after wrong answer (calls: ${initialCalls} -> ${this.startPlayTaskCalls})`,
      );
    });

    test('audio.stop is called before replay to clear isBusy guard', async function (assert) {
      const self = this;
      await render(
        <template><SingleSimpleWords @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;
      const wrongWord = task.answerOptions.find(o => o.word !== correctAnswer)?.word || 'cat';

      const initialStopCalls = this.stopCalls;

      await click(`[data-test-task-answer-option="${wrongWord}"]`);


      assert.true(
        this.stopCalls > initialStopCalls,
        `audio.stop was called before replay (stop calls: ${initialStopCalls} -> ${this.stopCalls})`,
      );
    });

    test('task is not completed after wrong answer, answer options remain', async function (assert) {
      let wrongAnswerCalled = false;
      this.set('onWrongAnswer', function () { wrongAnswerCalled = true; });
      const self = this;
      await render(
        <template><SingleSimpleWords @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;
      const wrongWord = task.answerOptions.find(o => o.word !== correctAnswer)?.word || 'cat';

      // Wrong answer - task should not complete, options should remain
      await click(`[data-test-task-answer-option="${wrongWord}"]`);


      assert.dom('[data-test-task-answer]').exists(
        'answer option buttons still visible after wrong answer (task not completed)',
      );
      assert.true(wrongAnswerCalled, 'onWrongAnswer callback was invoked');
      assert.notStrictEqual(
        document.body.dataset.correctAnswer,
        '',
        'correctAnswer is still set (still has sub-tasks to solve)',
      );
    });
  },
);
