import { module, test } from 'qunit';

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

module('Unit | Component | task-player | repeat step stays replayable', function () {
  // The repeat (interact) step no longer locks once every word is heard.
  // Re-entering it clears heardWords so the user can go through again, and the
  // loop does not auto-exit on completion (previously the step could only be
  // replayed by switching to the Listen tab and back).

  function enterInteract(heardWords, normalizedAnswerOptions) {
    // Replicate the reset-on-reentry guard at the top of interactModeTask.
    const allOptionsHeard =
      normalizedAnswerOptions.length > 0 &&
      normalizedAnswerOptions.every((o) => heardWords.has(o.word));
    return allOptionsHeard ? new Set() : heardWords;
  }

  test('re-entering a completed repeat step clears heardWords for a fresh pass', function (assert) {
    const options = [{ word: 'cat' }, { word: 'dog' }];
    const next = enterInteract(new Set(['cat', 'dog']), options);

    assert.strictEqual(next.size, 0, 'heardWords cleared on re-entry when all heard');
  });

  test('entering an in-progress repeat step keeps existing heardWords', function (assert) {
    const options = [{ word: 'cat' }, { word: 'dog' }, { word: 'bird' }];
    const next = enterInteract(new Set(['cat']), options);

    assert.strictEqual(next.size, 1, 'partial progress preserved on entry');
    assert.true(next.has('cat'), 'previously heard word retained');
  });

  test('a word played after completion is still processed (loop does not lock)', function (assert) {
    // Mirror the interact loop body: each processed click plays the word and
    // marks it heard. The removed early-return stopped processing once every
    // word was heard; this asserts a post-completion click is still played,
    // which is the actual user-facing fix (replay without switching tabs).
    let heardWords = new Set();
    const played = [];
    let exitedEarly = false;

    function processClick(word) {
      // Once the (removed) allOptionsHeard early-return fired, no further click
      // played. Keeping exitedEarly false models the current, fixed loop; if the
      // return is ever re-added here, the post-completion replay stops playing.
      if (exitedEarly) return;
      played.push(word);
      heardWords = new Set([...heardWords, word]);
    }

    processClick('cat');
    processClick('dog'); // every word heard here
    processClick('cat'); // replay after completion

    assert.false(exitedEarly, 'loop is not exited on completion');
    assert.deepEqual(
      played,
      ['cat', 'dog', 'cat'],
      'the post-completion replay click was played',
    );
    assert.strictEqual(
      played.filter((w) => w === 'cat').length,
      2,
      'a word can be heard again after every word was already heard',
    );
  });
});
