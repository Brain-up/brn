// @ts-nocheck -- QUnit test context typing not supported with @types/qunit v2.9
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, settled } from '@ember/test-helpers';
import { DateTime } from 'luxon';
import StatisticsWeekTimeTrack from 'brn/components/statistics/week-time-track';

module(
  'Integration | Component | statistics/week-time-track',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');

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

      const self = this;




      await render(<template><StatisticsWeekTimeTrack
      @isLoading={{self.isLoadingWeekTimeTrackData}}
      @selectedMonth={{self.selectedMonth}}
      @data={{self.rawWeekTimeTrackData}}
      /></template>);

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

      const self = this;




      await render(<template><StatisticsWeekTimeTrack
      @isLoading={{self.isLoadingWeekTimeTrackData}}
      @selectedMonth={{self.selectedMonth}}
      @data={{self.rawWeekTimeTrackData}}
      /></template>);

      const selectedMonth = DateTime.fromISO('2021-06-23');
      this.set('selectedMonth', selectedMonth);
      this.set('rawWeekTimeTrackData', TRACK_DATA);

      assert
        .dom('[data-test-empty-data]')
        .exists('empty data description is shown');
    });

    test('it uses localized weekday labels for en-us', async function (assert) {
      const TRACK_DATA = [
        {
          date: DateTime.fromISO('2021-06-07'), // Monday
          exercisingTimeSeconds: 600,
          progress: 'GOOD',
        },
        {
          date: DateTime.fromISO('2021-06-08'), // Tuesday
          exercisingTimeSeconds: 300,
          progress: 'BAD',
        },
      ];

      this.set('isLoadingWeekTimeTrackData', false);

      const self = this;

      await render(<template><StatisticsWeekTimeTrack
      @isLoading={{self.isLoadingWeekTimeTrackData}}
      @selectedMonth={{self.selectedMonth}}
      @data={{self.rawWeekTimeTrackData}}
      /></template>);

      const selectedMonth = DateTime.fromISO('2021-06-23');
      this.set('selectedMonth', selectedMonth);
      this.set('rawWeekTimeTrackData', TRACK_DATA);
      await settled();

      const tickTexts = this.element.querySelectorAll('.bb-axis-x .tick text');
      const labels = Array.from(tickTexts).map((el) => {
        const tspan = el.querySelector('tspan');
        return tspan ? tspan.textContent?.trim() : el.textContent?.trim();
      });

      const englishWeekdays = ['MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU'];
      const englishLabels = labels.filter((label) =>
        englishWeekdays.includes(label ?? ''),
      );
      // June 2021 has 30 days, so all 30 tick labels should be English weekday abbrevs
      assert.strictEqual(englishLabels.length, 30, 'all 30 day labels are English weekday abbreviations');
    });

    module('with ru-ru locale', function (hooks) {
      setupIntl(hooks, 'ru-ru');

      test('it uses localized weekday labels for ru-ru', async function (assert) {
        const TRACK_DATA = [
          {
            date: DateTime.fromISO('2021-06-07'), // Monday
            exercisingTimeSeconds: 600,
            progress: 'GOOD',
          },
          {
            date: DateTime.fromISO('2021-06-08'), // Tuesday
            exercisingTimeSeconds: 300,
            progress: 'BAD',
          },
        ];

        this.set('isLoadingWeekTimeTrackData', false);

        const self = this;

        await render(<template><StatisticsWeekTimeTrack
        @isLoading={{self.isLoadingWeekTimeTrackData}}
        @selectedMonth={{self.selectedMonth}}
        @data={{self.rawWeekTimeTrackData}}
        /></template>);

        const selectedMonth = DateTime.fromISO('2021-06-23');
        this.set('selectedMonth', selectedMonth);
        this.set('rawWeekTimeTrackData', TRACK_DATA);
        await settled();

        const tickTexts = this.element.querySelectorAll('.bb-axis-x .tick text');
        const labels = Array.from(tickTexts).map((el) => {
          const tspan = el.querySelector('tspan');
          return tspan ? tspan.textContent?.trim() : el.textContent?.trim();
        });

        const russianWeekdays = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ', 'ВС'];
        const russianLabels = labels.filter((label) =>
          russianWeekdays.includes(label ?? ''),
        );
        // June 2021 has 30 days, so all 30 tick labels should be Russian weekday abbrevs
        assert.strictEqual(russianLabels.length, 30, 'all 30 day labels are Russian weekday abbreviations');
      });
    });
  },
);
