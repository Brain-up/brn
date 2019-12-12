import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, settled } from '@ember/test-helpers';
import { timeout } from 'ember-concurrency';
import hbs from 'htmlbars-inline-precompile';
import { chooseAnswer } from './test-support/helpers';
import pageObject from './test-support/page-object';

module('Integration | Component | task-player', function(hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function() {
    const store = this.owner.lookup('service:store');
    const firstTask = store.createRecord('task', {
      id: '1',
      order: '1',
      word: 'бал',
      words: ['бал', 'бум', 'быль'],
      pictureFileUrl: 'path',
    });

    const secondTask = store.createRecord('task', {
      id: '2',
      order: '2',
      word: 'бал',
      words: ['бал', 'бум', 'быль'],
    });
    store.createRecord('exercise', { tasks: [firstTask, secondTask] });

    this.set('model', firstTask);

    await render(hbs`
    <TaskPlayer
      @task={{this.model}}/>
    `);
  });

  test('refreshes options list and shows regret image after a wrong answer', async function(assert) {
    const wrongAnswers = this.model.words.filter(
      (wordOption) => wordOption !== this.model.word,
    );
    const order = pageObject.options.mapBy('optionValue');

    chooseAnswer(wrongAnswers[0]);

    assert.notOk(pageObject.hasRightAnswer);

    await timeout(1500);

    assert
      .dom('[data-test-answer-correctness-widget]')
      .hasAttribute('data-test-isnt-correct');

    await settled();

    const newOrder = pageObject.options.mapBy('optionValue');

    assert.notDeepEqual(order, newOrder);
  });

  test('shows notification if answer is right and calls onRightAnswer', async function(assert) {
    assert.expect(2);
    this.set('onRightAnswer', function() {
      assert.ok(true, 'calls onRightAnswer');
    });

    await render(hbs`
      <TaskPlayer
        @onRightAnswer={{this.onRightAnswer}}
        @task={{this.model}}/>
      `);

    await chooseAnswer(this.model.word);
    assert.ok(pageObject.hasRightAnswer);
  });
});
