import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, settled } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import stubTaskInfo from './test-support/stub-task-info';
import {
  chooseAnswer,
  optionsHaveBeenShaffled,
  goToNextTask,
} from './test-support/helpers';
import pageObject from './test-support/page-object';

module('Integration | Component | task-player', function(hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function() {
    const store = this.owner.lookup('service:store');
    this.set('model', store.createRecord('task', stubTaskInfo));
    await render(hbs`
	<TaskPlayer
		@onFinished={{this.onFinished}}
		@task={{this.model}}/>
  `);
  });

  test('refreshes options list after a wrong answer', async function(assert) {
    const wrongAnswers = stubTaskInfo.words.filter(
      (wordOption) => wordOption !== stubTaskInfo.word,
    );
    const order = pageObject.options.mapBy('optionValue');

    await chooseAnswer(wrongAnswers[0]);

    assert.dom('[data-test-right-answer-notification]').doesNotExist();

    await settled();

    const newOrder = pageObject.options.mapBy('optionValue');
    assert.ok(
      optionsHaveBeenShaffled(order, newOrder),
      'options order changed',
    );
  });

  test('shows notification if answer is right and calls onFinished', async function(assert) {
    assert.expect(2);
    const store = this.owner.lookup('service:store');
    this.set('model', store.createRecord('task', stubTaskInfo));
    this.set('onFinished', function() {
      assert.ok(true, 'calls onFinished');
    });

    await render(hbs`
	<TaskPlayer
		@onFinished={{this.onFinished}}
		@task={{this.model}}/>
  `);

    await chooseAnswer(stubTaskInfo.word);
    assert.dom('[data-test-right-answer-notification]').exists();

    await goToNextTask();
  });
});
