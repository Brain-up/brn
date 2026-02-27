import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render, click, settled, waitFor } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import data from './test-support/data-storage';
import pageObject from './test-support/page-object';
import AudioService from 'brn/services/audio';

module('Integration | Component | words-seq-task-player', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  hooks.beforeEach(async function () {
    const store = this.owner.lookup('service:store');
    this.set('model', store.createRecord('task/words-sequences'));

    this.model.setProperties(data.task);

    await render(hbs`<TaskPlayer
      @task={{this.model}}
    />`);
  });

  test('it shows all the words', async function (assert) {
    const pageWords = pageObject.buttons.mapBy('word');

    Object.values(data.task.answerOptions)
      .reduce((array, subArray) => {
        array = array.concat(subArray);
        return array;
      }, [])
      .mapBy('word')
      .forEach((word) => {
        assert.ok(pageWords.includes(word), `word "${word}" is present`);
      });
  });
});

module('Integration | Component | words-seq-task-player | per-word correctness', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  hooks.beforeEach(async function () {
    class MockAudio extends AudioService {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      startPlayTask() {}
      audioUrlForText() { return ''; }
    }
    this.owner.register('service:audio', MockAudio);

    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task/words-sequences');
    model.setProperties({
      exerciseMechanism: 'MATRIX',
      type: 'task/MATRIX',
      name: '',
      wrongAnswers: [],
      template: '<OBJECT OBJECT_ACTION>',
      answerOptions: {
        OBJECT_ACTION: [
          { id: 345, audioFileUrl: '', word: 'линь', wordType: 'OBJECT_ACTION', pictureFileUrl: '', soundsCount: 0 },
          { id: 346, audioFileUrl: '', word: 'бал', wordType: 'OBJECT_ACTION', pictureFileUrl: '', soundsCount: 0 },
        ],
        OBJECT: [
          { id: 344, audioFileUrl: '', word: 'вить', wordType: 'OBJECT', pictureFileUrl: '', soundsCount: 0 },
          { id: 347, audioFileUrl: '', word: 'быль', wordType: 'OBJECT', pictureFileUrl: '', soundsCount: 0 },
        ],
      },
    });
    this.set('model', model);
    this.set('onRightAnswer', () => undefined);
    this.set('onWrongAnswer', () => undefined);
    this.set('onPlayText', () => undefined);
  });

  test('it marks each word individually as correct or incorrect when answer is partially wrong', async function (assert) {
    await render(hbs`
      <TaskPlayer::WordsSequences
        @task={{this.model}}
        @mode="task"
        @onRightAnswer={{this.onRightAnswer}}
        @onWrongAnswer={{this.onWrongAnswer}}
        @onPlayText={{this.onPlayText}}
      />
    `);

    // Read the correct answer set by the component on document.body
    const correctAnswer = document.body.dataset.correctAnswer.split(',');
    // correctAnswer[0] = correct OBJECT word, correctAnswer[1] = correct OBJECT_ACTION word
    const correctObjectWord = correctAnswer[0];
    const correctActionWord = correctAnswer[1];

    // Find a wrong word for OBJECT_ACTION type
    const allActionWords = this.model.answerOptions.OBJECT_ACTION.map(o => o.word);
    const wrongActionWord = allActionWords.find(w => w !== correctActionWord);

    // Click the CORRECT word for OBJECT type
    await click(`[data-test-task-answer-option="${correctObjectWord}"]`);

    // Click a WRONG word for OBJECT_ACTION type
    // Don't await — we need to inspect the DOM before handleWrongAnswer resets correctnessPerType
    click(`[data-test-task-answer-option="${wrongActionWord}"]`);

    // Wait for correctness indicators to appear
    await waitFor('.correctness-indicator', { timeout: 2000 });

    // Correct word should have green border
    assert.dom(`[data-test-task-answer-option="${correctObjectWord}"]`).hasClass(
      'border-green-500',
      'Correctly answered word should have green border'
    );

    // Wrong word should have red border
    assert.dom(`[data-test-task-answer-option="${wrongActionWord}"]`).hasClass(
      'border-red-500',
      'Incorrectly answered word should have red border'
    );

    // Let all pending async operations settle
    await settled();
  });
});
