import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { waitUntil } from '@ember/test-helpers';
import { destroy } from '@ember/destroyable';
import TaskPlayerComponent from 'brn/components/task-player';

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

module('Unit | Component | task-player | repeat step stays replayable', function (hooks) {
  // These tests drive the REAL TaskPlayerComponent and its interactModeTask —
  // not a simulation — so re-introducing the removed all-heard early-return
  // (the lock this PR fixes) makes them fail. The audio service records the
  // last requested text in `_lastText` ("for tests"), which is how we observe
  // that a click actually reached playback.
  setupTest(hooks);

  function makeComponent(owner, words) {
    return new TaskPlayerComponent(owner, {
      task: {
        normalizedAnswerOptions: words.map((word) => ({
          word,
          wordPronounce: word,
        })),
        usePreGeneratedAudio: false,
        exerciseMechanism: 'WORDS',
      },
    });
  }

  async function clickAndAwaitPlayback(component, word) {
    component.audio._lastText = null;
    component.playText(word);
    await waitUntil(() => component.audio._lastText === word, { timeout: 5000 });
    await waitUntil(() => component.heardWords.has(word), { timeout: 5000 });
  }

  async function stop(instance) {
    instance.cancel();
    await instance.catch(() => {
      // cancellation is expected
    });
  }

  test('a word can be replayed after every word was heard (loop stays alive)', async function (assert) {
    const component = makeComponent(this.owner, ['cat', 'dog']);
    const instance = component.interactModeTask.perform();

    await clickAndAwaitPlayback(component, 'cat');
    await clickAndAwaitPlayback(component, 'dog');
    assert.true(component.allOptionsHeard, 'every word has been heard');
    assert.true(
      component.interactModeTask.isRunning,
      'interact loop is still running after completion (no early exit)',
    );

    // The user clicks a word again — the loop must still process the click.
    component.audio._lastText = null;
    component.playText('cat');
    await waitUntil(() => component.audio._lastText === 'cat', { timeout: 5000 });
    assert.strictEqual(
      component.audio._lastText,
      'cat',
      'a post-completion click still plays the word (no lock)',
    );

    await stop(instance);
    destroy(component);
  });

  test('re-entering a completed repeat step clears heardWords for a fresh pass', async function (assert) {
    const component = makeComponent(this.owner, ['cat']);
    const first = component.interactModeTask.perform();
    await clickAndAwaitPlayback(component, 'cat');
    assert.true(component.allOptionsHeard, 'pass completed');
    await stop(first);

    const second = component.interactModeTask.perform();
    await waitUntil(() => component.heardWords.size === 0, { timeout: 5000 });
    assert.strictEqual(
      component.heardWords.size,
      0,
      'heardWords cleared on re-entry when all words were heard',
    );

    await stop(second);
    destroy(component);
  });

  test('entering an in-progress repeat step keeps existing heardWords', async function (assert) {
    const component = makeComponent(this.owner, ['cat', 'dog']);
    const first = component.interactModeTask.perform();
    await clickAndAwaitPlayback(component, 'cat');
    await stop(first);

    const second = component.interactModeTask.perform();
    // Let the task run its entry guard and a couple of poll cycles.
    await new Promise((resolve) => setTimeout(resolve, 400));
    assert.true(
      component.heardWords.has('cat'),
      'partial progress preserved on re-entry',
    );
    assert.strictEqual(component.heardWords.size, 1, 'nothing was cleared');

    await stop(second);
    destroy(component);
  });
});
