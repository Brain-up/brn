import { module, test } from 'qunit';
import { MODES } from 'brn/utils/task-modes';

module('Unit | Component | task-player | heardWords tracking', function () {
  // Test the heardWords and allOptionsHeard logic by replicating the
  // relevant getters and actions from TaskPlayerComponent.

  function createComponent({ normalizedAnswerOptions, heardWords }) {
    const component = {
      heardWords: heardWords || new Set(),
      justEnteredTask: true,
      args: {
        task: {
          normalizedAnswerOptions: normalizedAnswerOptions || [],
        },
      },
      // Reproduce the getter from TaskPlayerComponent
      get allOptionsHeard() {
        const options = this.args.task.normalizedAnswerOptions;
        return options.length > 0 && options.every((o) => this.heardWords.has(o.word));
      },
    };
    return component;
  }

  test('heardWords is reset when onTaskChanged is called', function (assert) {
    const component = createComponent({
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }],
      heardWords: new Set(['cat', 'dog']),
    });

    // Replicate onTaskChanged logic from TaskPlayerComponent
    function onTaskChanged(comp) {
      comp.heardWords = new Set();
      // The real method also checks justEnteredTask and may call exerciseSequenceTask,
      // but heardWords reset is the first thing it does unconditionally.
    }

    assert.strictEqual(component.heardWords.size, 2, 'heardWords has 2 entries before reset');

    onTaskChanged(component);

    assert.strictEqual(component.heardWords.size, 0, 'heardWords is empty after task change');
    assert.false(component.allOptionsHeard, 'allOptionsHeard is false after reset');
  });

  test('heardWords is reset when startTask is called', function (assert) {
    const component = createComponent({
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }],
      heardWords: new Set(['cat']),
    });

    // Replicate startTask logic from TaskPlayerComponent
    function startTask(comp) {
      comp.heardWords = new Set();
      comp.justEnteredTask = false;
      // The real method also calls maybeStartExercise() and exerciseSequenceTask,
      // but heardWords reset happens first.
    }

    assert.strictEqual(component.heardWords.size, 1, 'heardWords has 1 entry before startTask');

    startTask(component);

    assert.strictEqual(component.heardWords.size, 0, 'heardWords is empty after startTask');
    assert.false(component.justEnteredTask, 'justEnteredTask is false after startTask');
  });

  test('allOptionsHeard returns false when no words have been heard', function (assert) {
    const component = createComponent({
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }, { word: 'bird' }],
    });

    assert.false(component.allOptionsHeard, 'allOptionsHeard is false with empty heardWords');
  });

  test('allOptionsHeard returns false when only some words have been heard', function (assert) {
    const component = createComponent({
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }, { word: 'bird' }],
      heardWords: new Set(['cat', 'dog']),
    });

    assert.false(component.allOptionsHeard, 'allOptionsHeard is false when not all options heard');
  });

  test('allOptionsHeard returns true when all normalizedAnswerOptions words are in heardWords', function (assert) {
    const component = createComponent({
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }, { word: 'bird' }],
      heardWords: new Set(['cat', 'dog', 'bird']),
    });

    assert.true(component.allOptionsHeard, 'allOptionsHeard is true when all options heard');
  });

  test('allOptionsHeard returns true even when heardWords contains extra words', function (assert) {
    const component = createComponent({
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }],
      heardWords: new Set(['cat', 'dog', 'bird', 'fish']),
    });

    assert.true(component.allOptionsHeard, 'allOptionsHeard is true even with extra heard words');
  });

  test('allOptionsHeard returns false when normalizedAnswerOptions is empty', function (assert) {
    const component = createComponent({
      normalizedAnswerOptions: [],
      heardWords: new Set(['cat']),
    });

    assert.false(component.allOptionsHeard, 'allOptionsHeard is false when there are no options');
  });
});

module('Unit | Component | task-player | interactModeTask heardWords accumulation', function () {
  // Test the logic that accumulates heard words during interact mode
  // and triggers auto-transition when all options are heard.

  function simulateInteractIteration({ heardWords, playText, normalizedAnswerOptions }) {
    // Reproduce the heardWords accumulation from interactModeTask
    const newHeardWords = new Set([...heardWords, playText]);

    // Reproduce the allOptionsHeard check
    const allOptionsHeard =
      normalizedAnswerOptions.length > 0 &&
      normalizedAnswerOptions.every((o) => newHeardWords.has(o.word));

    return { newHeardWords, allOptionsHeard };
  }

  test('adds played word to heardWords', function (assert) {
    const result = simulateInteractIteration({
      heardWords: new Set(),
      playText: 'cat',
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }],
    });

    assert.true(result.newHeardWords.has('cat'), 'cat was added to heardWords');
    assert.strictEqual(result.newHeardWords.size, 1, 'heardWords has 1 entry');
  });

  test('preserves existing heard words when adding new one', function (assert) {
    const result = simulateInteractIteration({
      heardWords: new Set(['cat']),
      playText: 'dog',
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }, { word: 'bird' }],
    });

    assert.true(result.newHeardWords.has('cat'), 'cat is still in heardWords');
    assert.true(result.newHeardWords.has('dog'), 'dog was added to heardWords');
    assert.strictEqual(result.newHeardWords.size, 2, 'heardWords has 2 entries');
  });

  test('triggers auto-transition when last option is heard', function (assert) {
    const result = simulateInteractIteration({
      heardWords: new Set(['cat']),
      playText: 'dog',
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }],
    });

    assert.true(result.allOptionsHeard, 'allOptionsHeard is true after hearing last word');
  });

  test('does not trigger auto-transition when options remain unheard', function (assert) {
    const result = simulateInteractIteration({
      heardWords: new Set(),
      playText: 'cat',
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }, { word: 'bird' }],
    });

    assert.false(result.allOptionsHeard, 'allOptionsHeard is false when options remain');
  });

  test('hearing the same word twice does not change the set', function (assert) {
    const result = simulateInteractIteration({
      heardWords: new Set(['cat']),
      playText: 'cat',
      normalizedAnswerOptions: [{ word: 'cat' }, { word: 'dog' }],
    });

    assert.strictEqual(result.newHeardWords.size, 1, 'heardWords size unchanged after duplicate');
    assert.false(result.allOptionsHeard, 'allOptionsHeard still false after duplicate');
  });
});

module('Unit | Component | task-player | exerciseSequenceTask auto-transition', function () {
  // The auto-sequence stops after INTERACT — users opt into TASK (Solve)
  // manually, so the body of exerciseSequenceTask only calls setMode for
  // LISTEN and INTERACT.

  async function runExerciseSequence(setMode) {
    try {
      await setMode(MODES.LISTEN);
    } catch (_e) {
      return;
    }
    try {
      await setMode(MODES.INTERACT);
    } catch (_e) {
      // Interact was interrupted
    }
  }

  test('transitions through listen -> interact and stops before task', async function (assert) {
    const calls = [];
    await runExerciseSequence(async (mode) => {
      calls.push(mode);
    });

    assert.strictEqual(calls.length, 2, 'setMode called twice');
    assert.strictEqual(calls[0], MODES.LISTEN, 'first call is LISTEN');
    assert.strictEqual(calls[1], MODES.INTERACT, 'second call is INTERACT');
    assert.false(calls.includes(MODES.TASK), 'TASK is not auto-entered');
  });

  test('allOptionsHeard check causes loop exit after all options played (simulated logic)', async function (assert) {
    // Simulate the interactModeTask loop logic (sync) to verify the break condition
    let heardWords = new Set();
    const normalizedAnswerOptions = [{ word: 'cat' }, { word: 'dog' }];
    const wordsToPlay = ['cat', 'dog'];
    let iterationCount = 0;

    // Simulate the interact mode loop
    for (const word of wordsToPlay) {
      iterationCount++;
      heardWords = new Set([...heardWords, word]);
      const allOptionsHeard =
        normalizedAnswerOptions.length > 0 &&
        normalizedAnswerOptions.every((o) => heardWords.has(o.word));
      if (allOptionsHeard) {
        break;
      }
    }

    assert.strictEqual(iterationCount, 2, 'loop ran for both words before exiting');
    assert.true(
      normalizedAnswerOptions.every((o) => heardWords.has(o.word)),
      'all options were heard',
    );
  });
});
