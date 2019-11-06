import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, settled } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import { chooseAnswer, goToNextTask } from './test-support/helpers';
import pageObject from './test-support/page-object';
import deepEqual from 'brn/utils/deep-equal';

module('Integration | Component | task-player', function(hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function() {
    const store = this.owner.lookup('service:store');
    this.set(
      'model',
      store.createRecord('task', {
        order: '1',
        word: 'бал',
        words: ['бал', 'бум', 'быль'],
      }),
    );
    await render(hbs`
    <TaskPlayer
      @task={{this.model}}/>
    `);
  });

  test('refreshes options list after a wrong answer', async function(assert) {
    const wrongAnswers = this.model.words.filter(
      (wordOption) => wordOption !== this.model.word,
    );
    const order = pageObject.options.mapBy('optionValue');

    await chooseAnswer(wrongAnswers[0]);

    assert.dom('[data-test-right-answer-notification]').doesNotExist();

    await settled();

    const newOrder = pageObject.options.mapBy('optionValue');
    assert.ok(!deepEqual(order, newOrder), 'options order changed');
  });

  test('shows notification if answer is right and calls onFinished', async function(assert) {
    assert.expect(2);
    this.set('onFinished', function() {
      assert.ok(true, 'calls onFinished');
    });

    await render(hbs`
      <TaskPlayer
        @onFinished={{this.onFinished}}
        @task={{this.model}}/>
      `);

    await chooseAnswer(this.model.word);
    assert.dom('[data-test-right-answer-notification]').exists();

    await goToNextTask();
  });
});
