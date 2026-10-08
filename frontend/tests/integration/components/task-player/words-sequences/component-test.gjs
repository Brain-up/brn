import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import * as data from './test-support/data-storage';
import pageObject from './test-support/page-object';
import AudioService from 'brn/services/audio';
import TaskPlayer from 'brn/components/task-player';
import TaskPlayerWordsSequences from 'brn/components/task-player/words-sequences';

module('Integration | Component | words-seq-task-player', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  hooks.beforeEach(async function () {
    const store = this.owner.lookup('service:store');
    const taskData = Object.assign({}, data.task);
    delete taskData.type;
    this.set('model', store.createRecord('task/words-sequences', taskData));

    const self = this;




    await render(<template><TaskPlayer
    @task={{self.model}}
    /></template>);
  });

  test('it shows all the words', async function (assert) {
    const pageWords = pageObject.buttons.mapBy('word');

    Object.values(data.task.answerOptions)
      .reduce((array, subArray) => {
        array = array.concat(subArray);
        return array;
      }, [])
      .map((item) => item.word)
      .forEach((word) => {
        assert.ok(pageWords.includes(word), `word "${word}" is present`);
      });
  });
});

module('Integration | Component | words-seq-task-player | per-word correctness', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  hooks.beforeEach(async function () {
    class MockAudio extends AudioService {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      startPlayTask() {}
      audioUrlForText() { return ''; }
    }
    this.owner.register('service:audio', MockAudio);

    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task/words-sequences', {
      exerciseMechanism: 'MATRIX',
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

  test('it detects per-word correctness when answer is partially wrong', async function (assert) {
    const self = this;
    let wrongAnswerCount = 0;
    this.set('onWrongAnswer', () => { wrongAnswerCount++; });

    await render(<template><TaskPlayerWordsSequences
    @task={{self.model}}
    @mode="task"
    @onRightAnswer={{self.onRightAnswer}}
    @onWrongAnswer={{self.onWrongAnswer}}
    @onPlayText={{self.onPlayText}}
    /></template>);

    // Read the correct answer set by the component on document.body
    const correctAnswer = document.body.dataset.correctAnswer.split(',');
    // correctAnswer[0] = correct OBJECT word, correctAnswer[1] = correct OBJECT_ACTION word
    const correctObjectWord = correctAnswer[0];
    const correctActionWord = correctAnswer[1];

    // Find a wrong word for OBJECT_ACTION type
    const allActionWords = this.model.answerOptions.OBJECT_ACTION.map(o => o.word);
    const wrongActionWord = allActionWords.find(w => w !== correctActionWord);
    assert.ok(wrongActionWord, 'found a wrong action word to use');

    // Submit partially wrong answer: correct OBJECT word + wrong OBJECT_ACTION word
    await click(`[data-test-task-answer-option="${correctObjectWord}"]`);
    await click(`[data-test-task-answer-option="${wrongActionWord}"]`);

    // The component should detect the answer as wrong even though one word was correct
    assert.strictEqual(wrongAnswerCount, 1, 'onWrongAnswer is called when answer is partially wrong');

    // The same task should repeat (correct answer unchanged) because the answer was wrong
    const afterWrongCorrectAnswer = document.body.dataset.correctAnswer;
    assert.strictEqual(
      afterWrongCorrectAnswer,
      correctAnswer.join(','),
      'same task repeats after partially wrong answer'
    );

    // Both word types should still be available for selection (task was not advanced)
    assert.dom(`[data-test-task-answer-option="${correctObjectWord}"]`).exists(
      'correct OBJECT word is still available for retry'
    );
    assert.dom(`[data-test-task-answer-option="${wrongActionWord}"]`).exists(
      'wrong OBJECT_ACTION word is still available for retry'
    );

    // Now submit the fully correct answer to verify per-word differentiation
    await click(`[data-test-task-answer-option="${correctObjectWord}"]`);
    await click(`[data-test-task-answer-option="${correctActionWord}"]`);

    // onWrongAnswer should NOT have been called again — the fully correct answer
    // was accepted, proving the component differentiates per-word correctness
    assert.strictEqual(wrongAnswerCount, 1, 'onWrongAnswer was not called again for fully correct answer');
  });
});

module('Integration | Component | words-seq-task-player | progress stability on wrong answer', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  hooks.beforeEach(async function () {
    class MockAudio extends AudioService {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      startPlayTask() {}
      audioUrlForText() { return ''; }
    }
    this.owner.register('service:audio', MockAudio);

    const store = this.owner.lookup('service:store');
    const model = store.createRecord('task/words-sequences', {
      exerciseMechanism: 'MATRIX',
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
    this.wrongAnswerCount = 0;
    this.rightAnswerCount = 0;
    this.set('onRightAnswer', () => { this.rightAnswerCount++; });
    this.set('onWrongAnswer', () => { this.wrongAnswerCount++; });
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    this.set('onPlayText', function () {});
  });

  test('progress bar does not reset on wrong answer', async function (assert) {
    const self = this;




    await render(<template><TaskPlayerWordsSequences
    @task={{self.model}}
    @mode="task"
    @onRightAnswer={{self.onRightAnswer}}
    @onWrongAnswer={{self.onWrongAnswer}}
    @onPlayText={{self.onPlayText}}
    >
    <:header as |data|>
    <span data-test-tasks-count>{{data.tasks.length}}</span>
    </:header>
    </TaskPlayerWordsSequences></template>);

    // Record initial state
    const initialTasksCount = document.querySelector('[data-test-tasks-count]').textContent.trim();
    const initialCorrectAnswer = document.body.dataset.correctAnswer;
    assert.ok(initialCorrectAnswer, 'correct answer is set on body');
    assert.ok(parseInt(initialTasksCount) > 0, 'tasks count is positive');

    // Build a wrong answer: pick the correct OBJECT word but a wrong OBJECT_ACTION word
    const correctAnswer = initialCorrectAnswer.split(',');
    const correctObjectWord = correctAnswer[0];
    const correctActionWord = correctAnswer[1];
    const allActionWords = this.model.answerOptions.OBJECT_ACTION.map(o => o.word);
    const wrongActionWord = allActionWords.find(w => w !== correctActionWord);
    assert.ok(wrongActionWord, 'found a wrong action word to use');

    // Submit wrong answer
    await click(`[data-test-task-answer-option="${correctObjectWord}"]`);
    await click(`[data-test-task-answer-option="${wrongActionWord}"]`);

    // After wrong answer: tasks count should stay the same (progress denominator unchanged)
    const afterWrongTasksCount = document.querySelector('[data-test-tasks-count]').textContent.trim();
    assert.strictEqual(afterWrongTasksCount, initialTasksCount, 'tasksCopy length stays the same after wrong answer');

    // The same task should repeat (same correctAnswer)
    const afterWrongCorrectAnswer = document.body.dataset.correctAnswer;
    assert.strictEqual(afterWrongCorrectAnswer, initialCorrectAnswer, 'same task repeats after wrong answer');

    // onWrongAnswer should have been called once
    assert.strictEqual(this.wrongAnswerCount, 1, 'onWrongAnswer was called after the wrong answer');

    // Now submit the correct answer
    await click(`[data-test-task-answer-option="${correctObjectWord}"]`);
    await click(`[data-test-task-answer-option="${correctActionWord}"]`);

    // After correct answer: tasks count should remain the same (progress denominator unchanged)
    const afterCorrectTasksCount = document.querySelector('[data-test-tasks-count]').textContent.trim();
    assert.strictEqual(afterCorrectTasksCount, initialTasksCount, 'tasksCopy length stays the same after correct answer');

    // After correct answer: the next task should be presented (correctAnswer is set)
    const afterCorrectAnswer = document.body.dataset.correctAnswer;
    assert.ok(afterCorrectAnswer, 'a new correct answer is set after advancing');

    // onRightAnswer should NOT have been called (only 1 out of many tasks completed)
    assert.strictEqual(this.rightAnswerCount, 0, 'onRightAnswer is not called after completing just one task');
  });
});
