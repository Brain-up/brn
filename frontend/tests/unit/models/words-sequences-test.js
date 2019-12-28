import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

const TASK_DATA = {
  wrongAnswers: [],
  template: '<OBJECT OBJECT_ACTION>',
  answerOptions: {
    OBJECT_ACTION: [
      {
        id: 345,
        audioFileUrl: 'no_noise/линь.mp3',
        word: 'линь',
        wordType: 'OBJECT_ACTION',
        pictureFileUrl: 'pictures/линь.jpg',
        soundsCount: 0,
      },
      {
        id: 346,
        audioFileUrl: 'no_noise/бал.mp3',
        word: 'бал',
        wordType: 'OBJECT_ACTION',
        pictureFileUrl: 'pictures/бал.jpg',
        soundsCount: 0,
      },
    ],
    OBJECT: [
      {
        id: 344,
        audioFileUrl: 'no_noise/вить.mp3',
        word: 'вить',
        wordType: 'OBJECT',
        pictureFileUrl: 'pictures/вить.jpg',
        soundsCount: 0,
      },
      {
        id: 343,
        audioFileUrl: 'no_noise/быль.mp3',
        word: 'быль',
        wordType: 'OBJECT',
        pictureFileUrl: 'pictures/быль.jpg',
        soundsCount: 0,
      },
    ],
  },
};

module('Unit | Model | task/words-sequences', function(hooks) {
  setupTest(hooks);

  hooks.beforeEach(function() {
    let store = this.owner.lookup('service:store');
    let model = store.createRecord('task/words-sequences', TASK_DATA);
    this.set('model', model);
  });

  test('has right template order', function(assert) {
    assert.deepEqual(this.model.selectedItemsOrder, [
      'OBJECT',
      'OBJECT_ACTION',
    ]);
  });

  test('generates subtasks for all variants of a given template', function(assert) {
    assert.ok(this.model.tasksToSolve.length === 8);
  });

  test('adds wrong answers to the end of the sequence', function(assert) {
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
});
