/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click, settled, waitUntil } from '@ember/test-helpers';
import AudioService from 'brn/services/audio';
import StatsService from 'brn/services/stats';
import PhonemePairs from 'brn/components/task-player/phoneme-pairs';
import Prosody from 'brn/components/task-player/prosody';
import AuditorySequence from 'brn/components/task-player/auditory-sequence';
import EnvironmentalSounds from 'brn/components/task-player/environmental-sounds';
import SingleSimpleWords from 'brn/components/task-player/single-simple-words';

// Tests for the wrong answer visual feedback fix across all exercise task-player types.
//
// Bug: `isCorrect` was initialized as `false` and reset to `false` in `startTask()`,
// so when a wrong answer set it to `false` again, Ember's tracked property system
// detected no change and the template was never re-evaluated.
//
// Fix: Changed `isCorrect` from `boolean` to `boolean | null`, initialized as `null`,
// reset to `null` in `startTask()`. Now wrong answers trigger a `null -> false` change
// that Ember's tracking system detects, causing template re-evaluation.
//
// These integration tests render the REAL components and verify actual DOM feedback.
// The feedback is transient (shown briefly then reset by handleWrongAnswer -> startTask),
// so we use waitUntil to observe the DOM during the feedback window.

// Strip fields that SchemaRecord rejects (reserved 'type', non-schema 'wrongAnswers')
function schemaData(obj) {
  const copy = Object.assign({}, obj);
  delete copy.type;
  delete copy.wrongAnswers;
  return copy;
}

// ---------------------------------------------------------------------------
// 1. PhonemePairsComponent - bg-red-400 on wrong answer button
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-feedback | phoneme-pairs',
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
      class MockAudio extends AudioService {
        startPlayTask() {}
        stop() {}
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

    test('wrong answer button gets bg-red-400 class (transient feedback)', async function (assert) {
      let sawRedClass = false;
      this.set('onWrongAnswer', function () {
        // By the time onWrongAnswer fires, startTask has already reset isCorrect,
        // so we observe the red class earlier via the promise below.
      });

      const self = this;
      await render(
        <template><PhonemePairs @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // Fire the click without awaiting settled. The ember-concurrency task sets
      // isCorrect = false synchronously, then yields at customTimeout.
      // During that yield, the DOM should update with the red class.
      const clickPromise = click('[data-test-task-answer-option="пак"]');

      try {
        await waitUntil(
          () => {
            const el = document.querySelector('[data-test-task-answer-option="пак"]');
            if (el && el.classList.contains('bg-red-400')) {
              sawRedClass = true;
              return true;
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out if the class is never applied
      }

      assert.true(sawRedClass, 'wrong answer button briefly shows bg-red-400 class');

      await clickPromise;
    });

    test('correct answer button gets bg-green-500 class (transient feedback)', async function (assert) {
      let sawGreenClass = false;
      const self = this;
      await render(
        <template><PhonemePairs @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const clickPromise = click('[data-test-task-answer-option="бак"]');

      try {
        await waitUntil(
          () => {
            const el = document.querySelector('[data-test-task-answer-option="бак"]');
            if (el && el.classList.contains('bg-green-500')) {
              sawGreenClass = true;
              return true;
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out
      }

      assert.true(sawGreenClass, 'correct answer button briefly shows bg-green-500 class');

      await clickPromise;
    });

    test('no feedback class in initial state (isCorrect is null)', async function (assert) {
      const self = this;
      await render(
        <template><PhonemePairs @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      assert.dom('[data-test-task-answer-option="бак"]').doesNotHaveClass(
        'bg-red-400',
        'no red class in initial state',
      );
      assert.dom('[data-test-task-answer-option="бак"]').doesNotHaveClass(
        'bg-green-500',
        'no green class in initial state',
      );
    });
  },
);

// ---------------------------------------------------------------------------
// 2. ProsodyComponent - bg-red-400 on wrong answer button
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-feedback | prosody',
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
      class MockAudio extends AudioService {
        startPlayTask() {}
        stop() {}
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

    test('wrong answer button gets bg-red-400 class (transient feedback)', async function (assert) {
      let sawRedClass = false;
      const self = this;
      await render(
        <template><Prosody @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const clickPromise = click('[data-test-task-answer-option="Question?"]');

      try {
        await waitUntil(
          () => {
            const el = document.querySelector('[data-test-task-answer-option="Question?"]');
            if (el && el.classList.contains('bg-red-400')) {
              sawRedClass = true;
              return true;
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out
      }

      assert.true(sawRedClass, 'wrong answer button briefly shows bg-red-400 class');

      await clickPromise;
    });

    test('correct answer button gets bg-green-500 class (transient feedback)', async function (assert) {
      let sawGreenClass = false;
      const self = this;
      await render(
        <template><Prosody @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const clickPromise = click('[data-test-task-answer-option="Statement."]');

      try {
        await waitUntil(
          () => {
            const el = document.querySelector('[data-test-task-answer-option="Statement."]');
            if (el && el.classList.contains('bg-green-500')) {
              sawGreenClass = true;
              return true;
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out
      }

      assert.true(sawGreenClass, 'correct answer button briefly shows bg-green-500 class');

      await clickPromise;
    });

    test('no feedback class in initial state (isCorrect is null)', async function (assert) {
      const self = this;
      await render(
        <template><Prosody @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      assert.dom('[data-test-task-answer-option="Statement."]').doesNotHaveClass(
        'bg-red-400',
        'no red class in initial state',
      );
      assert.dom('[data-test-task-answer-option="Statement."]').doesNotHaveClass(
        'bg-green-500',
        'no green class in initial state',
      );
    });
  },
);

// ---------------------------------------------------------------------------
// 3. EnvironmentalSoundsComponent - bg-red-50 on options list container
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-feedback | environmental-sounds',
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
      class MockAudio extends AudioService {
        startPlayTask() {}
        stop() {}
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

    test('wrong answer applies bg-red-50 on options list container (transient feedback)', async function (assert) {
      let sawRedClass = false;
      const self = this;
      await render(
        <template><EnvironmentalSounds @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // Determine the correct answer from the component's data
      const correctAnswer = document.body.dataset.correctAnswer;
      // Find a wrong answer option
      const wrongWord = task.answerOptions.find(o => o.word !== correctAnswer)?.word
        || task.answerOptions.find(o => o.word !== 'rain')?.word;

      const clickPromise = click(`[data-test-task-answer-option="${wrongWord}"]`);

      try {
        await waitUntil(
          () => {
            const el = document.querySelector('[data-test-environmental-sounds-options]');
            if (el && el.classList.contains('bg-red-50')) {
              sawRedClass = true;
              return true;
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out
      }

      assert.true(sawRedClass, 'options list briefly shows bg-red-50 class on wrong answer');

      await clickPromise;
    });

    test('correct answer applies bg-green-50 on options list container (transient feedback)', async function (assert) {
      let sawGreenClass = false;
      const self = this;
      await render(
        <template><EnvironmentalSounds @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;
      const clickPromise = click(`[data-test-task-answer-option="${correctAnswer}"]`);

      try {
        await waitUntil(
          () => {
            const el = document.querySelector('[data-test-environmental-sounds-options]');
            if (el && el.classList.contains('bg-green-50')) {
              sawGreenClass = true;
              return true;
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out
      }

      assert.true(sawGreenClass, 'options list briefly shows bg-green-50 class on correct answer');

      await clickPromise;
    });

    test('no feedback class on options list in initial state', async function (assert) {
      const self = this;
      await render(
        <template><EnvironmentalSounds @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      assert.dom('[data-test-environmental-sounds-options]').doesNotHaveClass(
        'bg-red-50',
        'no red class in initial state',
      );
      assert.dom('[data-test-environmental-sounds-options]').doesNotHaveClass(
        'bg-green-50',
        'no green class in initial state',
      );
    });
  },
);

// ---------------------------------------------------------------------------
// 4. AuditorySequenceComponent - bg-red-100 / bg-green-100 on selection info
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-feedback | auditory-sequence',
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
      class MockAudio extends AudioService {
        startPlayTask() {}
        stop() {}
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

    test('wrong sequence applies bg-red-100 on selection info container (transient feedback)', async function (assert) {
      let sawRedClass = false;
      const self = this;
      await render(
        <template><AuditorySequence @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // Build wrong sequence
      const correctSequence = document.body.dataset.correctAnswer.split(',');
      const wrongSequence = [...correctSequence].reverse();

      if (wrongSequence.join(',') === correctSequence.join(',')) {
        assert.ok(true, 'skipped: cannot produce a wrong sequence from this data');
        return;
      }

      // Click all but the last word (they don't trigger the final check)
      for (let i = 0; i < wrongSequence.length - 1; i++) {
        await click(`[data-test-task-answer-option="${wrongSequence[i]}"]`);
      }

      // Fire the last click (triggers the comparison) without awaiting settled
      const lastClickPromise = click(`[data-test-task-answer-option="${wrongSequence[wrongSequence.length - 1]}"]`);

      try {
        await waitUntil(
          () => {
            const el = document.querySelector('[data-test-auditory-sequence-selection]');
            if (el && el.classList.contains('bg-red-100')) {
              sawRedClass = true;
              return true;
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out
      }

      assert.true(sawRedClass, 'selection info briefly shows bg-red-100 class on wrong sequence');

      await lastClickPromise;
    });

    test('correct sequence applies bg-green-100 on selection info container (transient feedback)', async function (assert) {
      let sawGreenClass = false;
      const self = this;
      await render(
        <template><AuditorySequence @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctSequence = document.body.dataset.correctAnswer.split(',');

      for (let i = 0; i < correctSequence.length - 1; i++) {
        await click(`[data-test-task-answer-option="${correctSequence[i]}"]`);
      }

      const lastClickPromise = click(`[data-test-task-answer-option="${correctSequence[correctSequence.length - 1]}"]`);

      try {
        await waitUntil(
          () => {
            const el = document.querySelector('[data-test-auditory-sequence-selection]');
            if (el && el.classList.contains('bg-green-100')) {
              sawGreenClass = true;
              return true;
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out
      }

      assert.true(sawGreenClass, 'selection info briefly shows bg-green-100 class on correct sequence');

      await lastClickPromise;
    });

    test('no selection info container in initial state', async function (assert) {
      const self = this;
      await render(
        <template><AuditorySequence @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      assert.dom('[data-test-auditory-sequence-selection]').doesNotExist(
        'no selection info container before any clicks (isCorrect is null)',
      );
    });
  },
);

// ---------------------------------------------------------------------------
// 5. SingleSimpleWordsComponent - option gets inline red/green style via didUpdate
// ---------------------------------------------------------------------------

module(
  'Integration | Component | task-player | wrong-answer-feedback | single-simple-words',
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
      class MockAudio extends AudioService {
        startPlayTask() {}
        stop() {}
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

    test('wrong answer button gets red inline background style via didUpdate (transient feedback)', async function (assert) {
      let sawRedBg = false;
      const self = this;
      await render(
        <template><SingleSimpleWords @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      // Find the wrong answer button (not the correct answer from the task)
      const correctAnswer = document.body.dataset.correctAnswer;
      const wrongWord = task.answerOptions.find(o => o.word !== correctAnswer)?.word || 'cat';

      const clickPromise = click(`[data-test-task-answer-option="${wrongWord}"]`);

      try {
        await waitUntil(
          () => {
            const el = document.querySelector(`[data-test-task-answer-option="${wrongWord}"]`);
            if (el) {
              const bg = el.style.backgroundColor;
              if (bg === 'rgb(243, 134, 152)' || bg === '#F38698' || bg === '#f38698') {
                sawRedBg = true;
                return true;
              }
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out
      }

      assert.true(sawRedBg, 'wrong answer button briefly shows red background (#F38698) via didUpdate');

      await clickPromise;
    });

    test('correct answer button gets green inline background style via didUpdate (transient feedback)', async function (assert) {
      let sawGreenBg = false;
      const self = this;
      await render(
        <template><SingleSimpleWords @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const correctAnswer = document.body.dataset.correctAnswer;
      const clickPromise = click(`[data-test-task-answer-option="${correctAnswer}"]`);

      try {
        await waitUntil(
          () => {
            const el = document.querySelector(`[data-test-task-answer-option="${correctAnswer}"]`);
            if (el) {
              const bg = el.style.backgroundColor;
              if (bg === 'rgb(71, 205, 138)' || bg === '#47CD8A' || bg === '#47cd8a') {
                sawGreenBg = true;
                return true;
              }
            }
            return false;
          },
          { timeout: 2000 },
        );
      } catch (_e) {
        // waitUntil may time out
      }

      assert.true(sawGreenBg, 'correct answer button briefly shows green background (#47CD8A) via didUpdate');

      await clickPromise;
    });

    test('no inline background style in initial state', async function (assert) {
      const self = this;
      await render(
        <template><SingleSimpleWords @task={{self.model}} @mode="task" @onRightAnswer={{self.onRightAnswer}} @onWrongAnswer={{self.onWrongAnswer}} /></template>
      );

      const btn = document.querySelector('[data-test-task-answer-option="cat"]');
      assert.ok(btn, 'button exists');
      assert.strictEqual(
        btn.style.backgroundColor,
        '',
        'no inline background color in initial state (isCorrect is null, didUpdate has not fired)',
      );
    });
  },
);
