import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import { DateTime } from 'luxon';
import { PROGRESS } from 'brn/models/user-weekly-statistics';
import sinon from 'sinon';
import click from '@ember/test-helpers/dom/click';
import Store from '@ember-data/store';

const generateTrackData = (
  store: Store,
  months: number[],
  year: number = 2021,
): any[] => {
  const result = [];
  const uniqueMonths = [...new Set(months)];
  if (uniqueMonths.length > 12 || uniqueMonths.length < months.length) {
    throw new Error(`Can't generate track data with params months=${months}`);
  }
  for (let i = 0; i < months.length; i++) {
    let month = months[i];
    const mm: string = (month < 10 ? '0' : '') + month;
    const trackItemData = store.createRecord('UserYearlyStatistics', {
      progress: PROGRESS.GREAT,
      date: DateTime.fromFormat(`${year}-${mm}-03`, 'yyyy-MM-dd'),
      exercisingDays: 5,
      exercisingTimeSeconds: 3600,
    });
    result.push(trackItemData);
  }
  return result;
};

module(
  'Integration | Component | statistics/month-time-track',
  function (hooks) {
    setupRenderingTest(hooks);

    hooks.beforeEach(function () {
      this.set('loadPrevYear', () => {});
      this.set('loadNextYear', () => {});
      this.set('selectMonth', () => {});
    });

    test('it renders from 0 to 12 months', async function (assert) {
      // Set any properties with this.set('myProperty', 'value');
      // Handle any actions with this.set('myAction', function(val) { ... });
      const store = this.owner.lookup('service:store');
      const currentYear: number = new Date().getFullYear();

      const TRACK_DATA_1 = generateTrackData(store, [6], currentYear); //June only
      const TRACK_DATA_12 = generateTrackData(
        store,
        [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
        currentYear,
      );
      const TRACK_DATA_0 = generateTrackData(store, [], currentYear);

      this.set('isLoadingMonthTimeTrackData', false);

      await render(hbs`<Statistics::MonthTimeTrack
        @isLoading={{this.isLoadingMonthTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.rawMonthTimeTrackData}}
        @onSelectMonth={{this.selectMonth}}
        @onLoadPrevYear={{this.loadPrevYear}}
        @onLoadNextYear={{this.loadNextYear}}
      />`);

      this.set('rawMonthTimeTrackData', TRACK_DATA_1);
      assert.dom('[data-test-month-track-item]').exists({ count: 1 });
      assert
        .dom('[data-test-month-incomplete-year]')
        .exists('year is incomplete');

      this.set('rawMonthTimeTrackData', TRACK_DATA_12);
      assert.dom('[data-test-month-track-item]').exists({ count: 12 });
      assert
        .dom('[data-test-month-incomplete-year]')
        .doesNotExist('year is complete');

      this.set('rawMonthTimeTrackData', TRACK_DATA_0);
      assert.dom('[data-test-month-track-item]').doesNotExist();
      assert
        .dom('[data-test-month-incomplete-year]')
        .exists('year is incomplete');
    });

    test('it selects month', async function (assert) {
      const store = this.owner.lookup('service:store');
      this.set('newSelectedMonth', null);

      const stubSelectMonth = (selectedMonth: DateTime) => {
        this.set('newSelectedMonth', selectedMonth);
      };

      this.set('selectMonth', stubSelectMonth);

      const currentYear: number = new Date().getFullYear();

      const TRACK_DATA_12 = generateTrackData(
        store,
        [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
        currentYear,
      );

      this.set('isLoadingMonthTimeTrackData', false);

      await render(hbs`<Statistics::MonthTimeTrack
        @isLoading={{this.isLoadingMonthTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.rawMonthTimeTrackData}}
        @onSelectMonth={{this.selectMonth}}
        @onLoadPrevYear={{this.loadPrevYear}}
        @onLoadNextYear={{this.loadNextYear}}
      />`);

      let selectedMonth = DateTime.fromISO(`${currentYear}-06-23`);
      this.set('selectedMonth', selectedMonth);

      this.set('rawMonthTimeTrackData', TRACK_DATA_12);
      assert
        .dom('[data-test-month-track-item]')
        .exists({ count: 12 }, '12 months are shown');
      assert.dom('[data-test-month-track-item-index="5"]').hasClass('selected');

      await click('[data-test-month-track-item-index="0"]');
      assert.equal(this.newSelectedMonth.month, 1, 'January is selected');

      await click('[data-test-month-track-item-index="3"]');
      assert.equal(this.newSelectedMonth.month, 4, 'April is selected');
    });

    test('it goes to prev/next year', async function (assert) {
      const store = this.owner.lookup('service:store');
      const stubLoadPrevYear = sinon.stub();
      const stubLoadNextYear = sinon.stub();

      this.set('loadPrevYear', stubLoadPrevYear);
      this.set('loadNextYear', stubLoadNextYear);

      const currentYear: number = new Date().getFullYear();
      const lastYear: number = currentYear - 1;

      const TRACK_DATA_0 = generateTrackData(store, [], currentYear);

      this.set('isLoadingMonthTimeTrackData', false);

      await render(hbs`<Statistics::MonthTimeTrack
        @isLoading={{this.isLoadingMonthTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.rawMonthTimeTrackData}}
        @onSelectMonth={{this.selectMonth}}
        @onLoadPrevYear={{this.loadPrevYear}}
        @onLoadNextYear={{this.loadNextYear}}
      />`);

      let selectedMonth = DateTime.fromISO(`${currentYear}-06-23`);
      this.set('selectedMonth', selectedMonth);

      this.set('rawMonthTimeTrackData', TRACK_DATA_0);

      assert.dom('[data-test-btn-next]').isDisabled('button next is disabled');
      assert.dom('[data-test-btn-prev]').isEnabled('button prev is enabled');

      selectedMonth = DateTime.fromISO(`${lastYear}-06-23`);
      this.set('selectedMonth', selectedMonth);

      assert.dom('[data-test-btn-next]').isEnabled('button next is enabled');
      assert.dom('[data-test-btn-prev]').isEnabled('button prev is enabled');

      await click('[data-test-btn-next]');
      assert.ok(stubLoadNextYear.calledOnce, 'loadNextYear is called');

      await click('[data-test-btn-prev]');
      assert.ok(stubLoadPrevYear.calledOnce, 'loadPrevYear is called');
    });

    test('it shows empty data', async function (assert) {
      const store = this.owner.lookup('service:store');
      const currentYear: number = new Date().getFullYear();

      const TRACK_DATA_1 = generateTrackData(store, [6], currentYear); //June only
      const TRACK_DATA_0 = generateTrackData(store, [], currentYear);

      this.set('isLoadingMonthTimeTrackData', false);

      await render(hbs`<Statistics::MonthTimeTrack
        @isLoading={{this.isLoadingMonthTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.rawMonthTimeTrackData}}
        @onSelectMonth={{this.selectMonth}}
        @onLoadPrevYear={{this.loadPrevYear}}
        @onLoadNextYear={{this.loadNextYear}}
      />`);

      let selectedMonth = DateTime.fromISO(`${currentYear}-06-23`);
      this.set('selectedMonth', selectedMonth);
      this.set('rawMonthTimeTrackData', TRACK_DATA_1);

      assert
        .dom('[data-test-empty-data]')
        .doesNotExist('no empty data description is shown');

      this.set('rawMonthTimeTrackData', TRACK_DATA_0);
      assert
        .dom('[data-test-empty-data]')
        .exists('empty data description is shown');
    });

    test('it shows loading spinner', async function (assert) {
      this.set('isLoadingMonthTimeTrackData', true);

      await render(hbs`<Statistics::MonthTimeTrack
        @isLoading={{this.isLoadingMonthTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.rawMonthTimeTrackData}}
        @onSelectMonth={{this.selectMonth}}
        @onLoadPrevYear={{this.loadPrevYear}}
        @onLoadNextYear={{this.loadNextYear}}
      />`);

      assert.dom('.loader').exists('loading spinner exists');

      this.set('isLoadingMonthTimeTrackData', false);

      assert.dom('.loader').doesNotExist("loading spinner doesn't exist");
    });
  },
);
