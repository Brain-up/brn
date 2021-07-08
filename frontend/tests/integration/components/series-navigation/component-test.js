import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';

const TEST_EXERCISE_NAMES = ['Type 1', 'Type 2'];

module('Integration | Component | series-navigation', function (hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function () {
    const store = this.owner.lookup('service:store');
    const testSeries = store.createRecord('series');
    const exercises = TEST_EXERCISE_NAMES.map((name, index) =>
      store.createRecord('exercise', {
        name,
        id: index,
        order: TEST_EXERCISE_NAMES.length - index,
        series: testSeries,
        tasks: [
          store.createRecord('task', [
            {
              order: '1',
              word: 'бал',
            },
          ]),
        ],
      }),
    );

    this.set('exercises', exercises);

    await render(hbs`<SeriesNavigation
      @exercises={{this.exercises}}/>`);
  });

  test('renders all exercises', async function (assert) {
    assert.deepEqual(pageObject.headers.length, 2);
    assert.equal(pageObject.links.length, 2);
  });
});
