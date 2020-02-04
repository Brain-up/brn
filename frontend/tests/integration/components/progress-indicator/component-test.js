import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, waitFor } from '@ember/test-helpers';
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
  });

  test('renders all progress items', async function(assert) {
    await render(hbs`<ProgressIndicator
      @progressItems={{this.tasks}}
    />`);
    await waitFor('[data-test-inidicator-root]');
    assert.equal(
      pageObject.indicatorItemsCount,
      3,
      'has three indicator items',
    );
    assert.deepEqual(pageObject.progressIndicators.mapBy('indicatorNum'), [
      '3',
      '2',
      '1',
    ]);
  });

  test('moves comleted to the right', async function(assert) {
    await render(hbs`<ProgressIndicator
      @progressItems={{this.tasks}}
    />`);
    await waitFor('[data-test-inidicator-root]');

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
    await render(hbs`<ProgressIndicator
      @progressItems={{this.tasks}}
    />`);
    await waitFor('[data-test-inidicator-root]');

    assert.ok(pageObject.shadedItems.includes('3'), 'the third item is shaded');
    assert.ok(
      pageObject.shadedItems.includes('2'),
      'the second item is shaded',
    );

    assert.notOk(
      pageObject.shadedItems.includes('1'),
      'current item is not shaded',
    );

    completeByOrder(this.tasks, 1);
    await customTimeout(1000);

    assert.notOk(
      pageObject.shadedItems.includes('2'),
      'the second item is not shaded(became current)',
    );
    assert.ok(pageObject.shadedItems.includes('3'), 'the third item is shaded');

    completeByOrder(this.tasks, 2);
    await customTimeout(1000);

    assert.notOk(pageObject.hasAnyShadedItems, 'the third item is not shaded');
  });

  test('hides excessive items', async function(assert) {
    this.set('longList', getLongItemsList());
    await render(hbs`<ProgressIndicator
      @progressItems={{this.longList}}
    />`);
    await waitFor('[data-test-inidicator-root]');
    completeByOrder(this.longList, 1);
    completeByOrder(this.longList, 2);
    completeByOrder(this.longList, 3);
    completeByOrder(this.longList, 4);
    completeByOrder(this.longList, 5);
    completeByOrder(this.longList, 6);
    completeByOrder(this.longList, 7);

    await customTimeout(100);

    assert.equal(
      pageObject.hiddenUncompletedIndicatorValue,
      `+${100 - pageObject.maxItemsAmount}`,
      'shows a right amount of hidden uncompleted items',
    );
    assert.equal(
      pageObject.hiddenCompletedIndicatorValue,
      '',
      'there are no completed hidden items',
    );
    for (let index = 8; index <= pageObject.maxItemsAmount / 2 + 1; index++) {
      completeByOrder(this.longList, index);
    }
    await customTimeout(100);
    assert.equal(
      pageObject.hiddenCompletedIndicatorValue,
      '+1',
      'there is one hidden completed item',
    );
  });
});
