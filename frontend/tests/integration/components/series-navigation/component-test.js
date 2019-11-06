import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

const TEST_EXERCISE_NAMES = ['Type1', 'Type2'];

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
    assert
      .dom('[data-test-series-navigation-list]')
      .exists({ count: TEST_EXERCISE_NAMES.length });
  });
});
