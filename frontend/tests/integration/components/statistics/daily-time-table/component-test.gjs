import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import Service from '@ember/service';
import StatisticsDailyTimeTable from 'brn/components/statistics/daily-time-table';

module('Integration | Component | statistics/daily-time-table', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  hooks.beforeEach(function () {
    // Mock store service that returns empty results by default
    class MockStore extends Service {
      query() {
        return Promise.resolve([]);
      }
    }
    this.owner.register('service:store', MockStore);
  });

  test('it renders a table after loading completes', async function (assert) {
    this.set('day', '2024-01-15');

    const self = this;




    await render(<template><StatisticsDailyTimeTable @day={{self.day}} /></template>);


    assert.dom('table').exists('renders a table element');
    assert.dom('thead').exists('table has a header');
  });

  test('it shows table column headers', async function (assert) {
    this.set('day', '2024-01-15');

    const self = this;




    await render(<template><StatisticsDailyTimeTable @day={{self.day}} /></template>);


    assert.dom('th').exists({ count: 6 }, 'renders 6 column headers');
  });

  test('it renders no data rows when query returns empty', async function (assert) {
    this.set('day', '2024-01-15');

    const self = this;




    await render(<template><StatisticsDailyTimeTable @day={{self.day}} /></template>);


    assert.dom('tbody tr').doesNotExist('no data rows when result is empty');
  });

  test('it renders data rows when query returns results', async function (assert) {
    class MockStoreWithData extends Service {
      query() {
        return Promise.resolve([
          {
            seriesName: 'Series 1',
            allDoneExercises: 10,
            uniqueDoneExercises: 5,
            repeatedExercises: 5,
            doneExercisesSuccessfullyFromFirstTime: 3,
            listenWordsCount: 20,
          },
          {
            seriesName: 'Series 2',
            allDoneExercises: 8,
            uniqueDoneExercises: 4,
            repeatedExercises: 4,
            doneExercisesSuccessfullyFromFirstTime: 2,
            listenWordsCount: 15,
          },
        ]);
      }
    }
    this.owner.register('service:store', MockStoreWithData);

    this.set('day', '2024-01-15');

    const self = this;




    await render(<template><StatisticsDailyTimeTable @day={{self.day}} /></template>);


    assert.dom('tbody tr').exists({ count: 2 }, 'renders 2 data rows');
  });
});
