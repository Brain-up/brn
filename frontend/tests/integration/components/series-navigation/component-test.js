import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';

const TEST_EXERCISE_NAMES = ['Type 1', 'Type 2'];

module('Integration | Component | series-navigation', function(hooks) {
  setupRenderingTest(hooks);

  hooks.beforeEach(async function() {
    const store = this.owner.lookup('service:store');
    const series = store.createRecord('series', {
      name: 'распознование слов',
      exercises: TEST_EXERCISE_NAMES.map((name, index) =>
        store.createRecord('exercise', {
          name,
          id: index,
          order: TEST_EXERCISE_NAMES.length - index,
          tasks: [
            store.createRecord('task', [
              {
                order: '1',
                word: 'бал',
              },
            ]),
          ],
        }),
      ),
    });
    this.set('series', series);

    await render(hbs`<SeriesNavigation
      @series={{this.series}}/>`);
  });

  test('renders all exercises', async function(assert) {
    assert.equal(pageObject.links.length, 2);
  });

  test('renders according to order', async function(assert) {
    assert.deepEqual(pageObject.links.mapBy('text'), ['Type 2', 'Type 1']);
  });
});
