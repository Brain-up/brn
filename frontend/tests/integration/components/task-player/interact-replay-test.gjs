import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, waitUntil } from '@ember/test-helpers';
import TaskPlayerComponent from 'brn/components/task-player';

// The real TaskPlayerComponent, rendered for real. Glimmer components cannot
// be constructed manually (the base constructor asserts on the manager-created
// args proxy), so a capturing subclass hands the test the live instance.
let lastInstance = null;
class CapturingTaskPlayer extends TaskPlayerComponent {
  constructor(owner, args) {
    super(owner, args);
    lastInstance = this;
  }
}

function makeTask(words) {
  return {
    exerciseMechanism: 'WORDS',
    usePreGeneratedAudio: false,
    pauseExecution: false,
    normalizedAnswerOptions: words.map((word) => ({
      word,
      wordPronounce: word,
    })),
    // columnNumber -1 → sortedAnswerOptions skips column grouping entirely
    answerOptions: words.map((word) => ({
      word,
      wordPronounce: word,
      columnNumber: -1,
    })),
    tasksToSolve: words.map((word, order) => ({
      order,
      answer: [{ word, wordPronounce: word }],
    })),
    exercise: {
      wordsColumns: Math.max(words.length, 1),
      isStarted: true,
      trackTime() {},
    },
  };
}

module('Integration | Component | task-player | repeat step stays replayable', function (hooks) {
  // These tests drive the REAL interactModeTask, so re-introducing the removed
  // all-heard early-return (the lock this PR fixes) makes them fail.
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  // Words the interact loop actually sent to playback. Patching the audio
  // service (the established pattern in the audio service tests) keeps the
  // observation exact: render-time getters also build audio URLs, but only
  // playback code calls setAudioElements/playAudio.
  let playedWords;

  hooks.beforeEach(function () {
    lastInstance = null;
    playedWords = [];
    const audio = this.owner.lookup('service:audio');
    // playAudio is an @action — a getter-only accessor on the prototype — so
    // plain assignment throws; defineProperty shadows it with an own value.
    Object.defineProperty(audio, 'setAudioElements', {
      configurable: true,
      value: async (elements) => {
        const text = new URL(String(elements[0])).searchParams.get('text');
        if (text) {
          playedWords.push(text);
        }
      },
    });
    Object.defineProperty(audio, 'playAudio', {
      configurable: true,
      value: async () => {},
    });
  });

  // waitUntil is used instead of settled-aware helpers on purpose: the
  // interact loop keeps a run-loop timer alive by design (it polls for
  // clicks), so awaiting settled() while it runs would hang the test.
  async function clickAndAwaitPlayback(component, word) {
    const before = playedWords.filter((w) => w === word).length;
    component.playText(word);
    await waitUntil(
      () => playedWords.filter((w) => w === word).length > before,
      { timeout: 5000 },
    );
    await waitUntil(() => component.heardWords.has(word), { timeout: 5000 });
  }

  async function stop(instance) {
    instance.cancel();
    await instance.catch(() => {
      // cancellation is expected
    });
  }

  test('a word can be replayed after every word was heard (loop stays alive)', async function (assert) {
    const task = makeTask(['cat', 'dog']);
    await render(<template><CapturingTaskPlayer @task={{task}} /></template>);
    const component = lastInstance;
    assert.ok(component, 'captured the real component instance');

    const instance = component.interactModeTask.perform();
    await clickAndAwaitPlayback(component, 'cat');
    await clickAndAwaitPlayback(component, 'dog');
    assert.true(component.allOptionsHeard, 'every word has been heard');
    assert.true(
      component.interactModeTask.isRunning,
      'interact loop is still running after completion (no early exit)',
    );

    // The user clicks a word again — the loop must still process the click.
    await clickAndAwaitPlayback(component, 'cat');
    assert.strictEqual(
      playedWords.filter((w) => w === 'cat').length,
      2,
      'a post-completion click still plays the word (no lock)',
    );

    await stop(instance);
  });

  test('re-entering a completed repeat step clears heardWords for a fresh pass', async function (assert) {
    const task = makeTask(['cat']);
    await render(<template><CapturingTaskPlayer @task={{task}} /></template>);
    const component = lastInstance;

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
  });

  test('entering an in-progress repeat step keeps existing heardWords', async function (assert) {
    const task = makeTask(['cat', 'dog']);
    await render(<template><CapturingTaskPlayer @task={{task}} /></template>);
    const component = lastInstance;

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
  });
});
