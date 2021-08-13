import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import { DateTime } from 'luxon';

module(
  'Integration | Component | statistics/week-time-track',
  function (hooks) {
    setupRenderingTest(hooks);

    test('it renders', async function (assert) {
      const TRACK_DATA = [
        {
          date: DateTime.fromISO('2021-06-08'),
          exercisingTimeSeconds: 15 * 60,
          progress: 'GOOD',
        },
        {
          date: DateTime.fromISO('2021-06-10'),
          exercisingTimeSeconds: 190,
          progress: 'BAD',
        },
        {
          date: DateTime.fromISO('2021-06-11'),
          exercisingTimeSeconds: 25 * 60,
          progress: 'GREAT',
        },
      ];

      this.set('isLoadingWeekTimeTrackData', false);

      await render(hbs`<Statistics::WeekTimeTrack
        @isLoading={{this.isLoadingWeekTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.rawWeekTimeTrackData}}        
      />`);

      const selectedMonth = DateTime.fromISO('2021-06-23');
      this.set('selectedMonth', selectedMonth);

      this.set('rawWeekTimeTrackData', TRACK_DATA);

      assert
        .dom('[data-test-empty-data]')
        .doesNotExist('no empty data description is shown');
      assert.dom('#chart').exists();
    });

    test('it shows empty data', async function (assert) {
      const TRACK_DATA: any[] = [];

      this.set('isLoadingWeekTimeTrackData', false);

      await render(hbs`<Statistics::WeekTimeTrack
        @isLoading={{this.isLoadingWeekTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.rawWeekTimeTrackData}}        
      />`);

      const selectedMonth = DateTime.fromISO('2021-06-23');
      this.set('selectedMonth', selectedMonth);
      this.set('rawWeekTimeTrackData', TRACK_DATA);

      assert
        .dom('[data-test-empty-data]')
        .exists('empty data description is shown');
    });
  },
);
