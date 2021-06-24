import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { pauseTest, render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import { DateTime } from 'luxon';
import { PROGRESS } from 'brn/models/user-weekly-statistics';

const generateTrackData = (
  monthCount: number,
  startFromMonth: number = 1,
): any[] => {
  const result = [];
  if (monthCount + startFromMonth - 1 > 12) {
    throw new Error(
      `Can't generate track data with params monthCount=${monthCount}, startFromMonth=${startFromMonth}`,
    );
  }
  for (let i = 0; i < monthCount; i++) {
    let month = startFromMonth + i;
    const mm: string = (month < 10 ? '0' : '') + month;
    const trackItemData = {
      progress: PROGRESS.GREAT,
      date: `2021-${mm}`,
      exercisingDays: 5,
      exercisingTimeSeconds: 3600,
    };
    result.push(trackItemData);
  }
  return result;
};

module(
  'Integration | Component | statistics/month-time-track',
  function (hooks) {
    setupRenderingTest(hooks);

    const noop = () => {};

    const TRACK_DATA_1 = generateTrackData(1, 3);

    const TRACK_DATA_12 = generateTrackData(12);

    const TRACK_DATA_0 = generateTrackData(0);

    test('it renders from 0 to 12 items', async function (assert) {
      // Set any properties with this.set('myProperty', 'value');
      // Handle any actions with this.set('myAction', function(val) { ... });

      this.set('loadPrevYear', noop);
      this.set('loadNextYear', noop);
      this.set('selectMonth', () => {});

      this.set('isLoadingMonthTimeTrackData', false);

      await render(hbs`<Statistics::MonthTimeTrack
        @isLoading={{this.isLoadingMonthTimeTrackData}}
        @selectedMonth={{this.selectedMonth}}
        @data={{this.rawMonthTimeTrackData}}
        @onSelectMonth={{this.selectMonth}}
        @onLoadPrevYear={{this.loadPrevYear}}
        @onLoadNextYear={{this.loadNextYear}}
      />`);

      let selectedMonth = DateTime.fromISO('2021-06-23');
      this.set('selectedMonth', selectedMonth);
      this.set('rawMonthTimeTrackData', TRACK_DATA_1);
      assert.dom('[data-test-month-track-item]').exists({ count: 1 });

      this.set('rawMonthTimeTrackData', TRACK_DATA_12);
      assert.dom('[data-test-month-track-item]').exists({ count: 12 });
      assert.dom('[data-test-month-track-item-index="5"]').hasClass('selected');

      this.set('rawMonthTimeTrackData', TRACK_DATA_0);
      assert.dom('[data-test-month-track-item]').doesNotExist();
    });
  },
);
