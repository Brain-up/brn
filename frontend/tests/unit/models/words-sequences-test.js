import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

const TASK_DATA = {
  wrongAnswers: [],
  template: '<OBJECT OBJECT_ACTION>',
  answerOptions: {
    OBJECT_ACTION: [
      {
        id: 345,
        audioFileUrl: '',
        word: 'линь',
        wordType: 'OBJECT_ACTION',
        pictureFileUrl: 'pictures/линь.jpg',
        soundsCount: 0,
      },
      {
        id: 346,
        audioFileUrl: '',
        word: 'бал',
        wordType: 'OBJECT_ACTION',
        pictureFileUrl: 'pictures/бал.jpg',
        soundsCount: 0,
      },
    ],
    OBJECT: [
      {
        id: 344,
        audioFileUrl: '',
        word: 'вить',
        wordType: 'OBJECT',
        pictureFileUrl: 'pictures/вить.jpg',
        soundsCount: 0,
      },
      {
        id: 343,
        audioFileUrl: '',
        word: 'быль',
        wordType: 'OBJECT',
        pictureFileUrl: 'pictures/быль.jpg',
        soundsCount: 0,
      },
    ],
  },
};

module('Unit | Model | task/words-sequences', function (hooks) {
  setupTest(hooks);

  hooks.beforeEach(function () {
    let store = this.owner.lookup('service:store');
    let model = store.createRecord('task/words-sequences', TASK_DATA);
    this.set('model', model);
  });

  test('has right template order', function (assert) {
    assert.deepEqual(this.model.selectedItemsOrder, [
      'OBJECT',
      'OBJECT_ACTION',
    ]);
  });

  test('generates subtasks for all variants of a given template', function (assert) {
    assert.ok(this.model.tasksToSolve.length === 8);
  });

  test('adds wrong answers to the end of the sequence', function (assert) {
    this.model.wrongAnswers = [].concat([
      {
        answer: {
          OBJECT: TASK_DATA.answerOptions.OBJECT[0],
          OBJECT_ACTION: TASK_DATA.answerOptions.OBJECT_ACTION[0],
        },
      },
    ]);
    assert.ok(this.model.tasksToSolve.length === 9);
  });

  test('tasksToSolve grows when wrongAnswers is mutated via pushObject — documents why updateLocalTasks must not be called in handleWrongAnswer', function (assert) {
    const initialLength = this.model.tasksToSolve.length;
    assert.strictEqual(initialLength, 8, 'initial tasksToSolve has 8 items');

    // Simulate what handleWrongAnswer does: push a wrong answer onto the array
    this.model.wrongAnswers.pushObject({
      answer: {
        OBJECT: TASK_DATA.answerOptions.OBJECT[0],
        OBJECT_ACTION: TASK_DATA.answerOptions.OBJECT_ACTION[0],
      },
    });

    // After pushObject, tasksToSolve recalculates and grows by 1
    // This is the root cause of the progress bar regression: if updateLocalTasks()
    // is called after pushObject, it rebuilds tasksCopy with the longer array, shrinking
    // the progress ratio (completed / total).
    const newLength = this.model.tasksToSolve.length;
    assert.strictEqual(newLength, initialLength + 1, 'tasksToSolve grows by 1 after pushObject to wrongAnswers');
  });
});
