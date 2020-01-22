import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, clearRender } from '@ember/test-helpers';
import customTimeout from 'brn/utils/custom-timeout';
import hbs from 'htmlbars-inline-precompile';
import { createStubTasks } from 'brn/tests/test-support/general-helpers';
import { completeByOrder, getLongItemsList } from './test-support/helpers';
import pageObject from './test-support/page-object';

module('Integration | Component | progress-indicator', function(hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function() {
    const store = this.owner.lookup('service:store');
    this.set(
      'tasks',
      createStubTasks(store, [
        {
          order: 1,
          word: 'бал',
        },
        {
          order: 2,
          word: 'бум',
        },
        {
          order: 3,
          word: 'быль',
        },
      ]),
    );
    await render(hbs`<ProgressIndicator
      @progressItems={{this.tasks}}
    />`);
  });

  test('renders all progress items', async function(assert) {
    assert.dom('[data-test-progress-indicator-item]').exists({ count: 3 });
    assert.deepEqual(pageObject.progressIndicators.mapBy('indicatorNum'), [
      '3',
      '2',
      '1',
    ]);
  });

  test('moves comleted to the right', async function(assert) {
    completeByOrder(this.tasks, 1);
    completeByOrder(this.tasks, 2);
    completeByOrder(this.tasks, 3);
    await customTimeout(1000);
    assert.ok(
      pageObject.progressIndicators
        .mapBy('style')
        .every((style) => /transform: translateX/.test(style)),
    );
  });

  test('shades items except of completed items and the one that is currently in progress', async function(assert) {
    assert
      .dom('[data-test-progress-indicator-item-number="3"] span')
      .hasAttribute('data-test-shaded-progress-circle-element');
    assert
      .dom('[data-test-progress-indicator-item-number="2"] span')
      .hasAttribute('data-test-shaded-progress-circle-element');

    completeByOrder(this.tasks, 1);
    await customTimeout(1000);

    assert
      .dom('[data-test-progress-indicator-item-number="2"] span')
      .doesNotHaveAttribute('data-test-shaded-progress-circle-element');

    completeByOrder(this.tasks, 2);
    await customTimeout(1000);

    assert
      .dom('[data-test-progress-indicator-item-number="3"] span')
      .doesNotHaveAttribute('data-test-shaded-progress-circle-element');
  });

  test('hides excessive items', async function(assert) {
    await clearRender();

    this.set('longList', getLongItemsList());

    await render(hbs`<ProgressIndicator
      @progressItems={{this.longList}}
    />`);
    completeByOrder(this.longList, 1);
    completeByOrder(this.longList, 2);
    completeByOrder(this.longList, 3);
    completeByOrder(this.longList, 4);
    completeByOrder(this.longList, 5);
    completeByOrder(this.longList, 6);
    completeByOrder(this.longList, 7);
    await customTimeout();

    assert
      .dom('[data-test-hidden-uncompleted]')
      .hasText(`+${100 - pageObject.maxItemsAmount}`);
    assert.dom('[data-test-hidden-completed]').hasText('');

    for (let index = 8; index <= pageObject.maxItemsAmount / 2 + 1; index++) {
      completeByOrder(this.longList, index);
    }
    await customTimeout();

    assert.dom('[data-test-hidden-completed]').hasText('+1');
  });
});
