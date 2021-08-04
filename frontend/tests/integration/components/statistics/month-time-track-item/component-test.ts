import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import { PROGRESS } from 'brn/models/user-weekly-statistics';
import { DateTime } from 'luxon';

module(
  'Integration | Component | statistics/month-time-track-item',
  function (hooks) {
    setupRenderingTest(hooks);

    test('it renders', async function (assert) {
      // Set any properties with this.set('myProperty', 'value');
      // Handle any actions with this.set('myAction', function(val) { ... });
      const serviceIntl = this.owner.lookup('service:intl');

      const itemData: any = {
        progress: PROGRESS.BAD,
        time: '02:45:23',
        days: 5,
        month: 'June',
        year: 2021,
        date: DateTime.fromISO('2021-06-23'),
      };

      this.set('itemData', itemData);

      await render(
        hbs`<Statistics::MonthTimeTrackItem @data={{this.itemData}} />`,
      );

      assert
        .dom('[data-test-month-track-item]')
        .hasNoClass('selected', "doesn't selected by default");
      assert.dom('[data-test-calendar]').hasClass('bg-PROGRESS-BAD');
      assert.dom('.time').hasText('02:45:23');
      assert.dom('.month').hasText('June');
      assert.dom('.days').hasText(
        serviceIntl.t('profile.statistics.month_time_track.days_period', {
          days: 5,
        }),
      );
    });

    test('it can be selected', async function (assert) {
      // Set any properties with this.set('myProperty', 'value');
      // Handle any actions with this.set('myAction', function(val) { ... });

      const itemData: any = {
        progress: PROGRESS.BAD,
        time: '02:45:23',
        days: 5,
        month: 'June',
        year: 2021,
        date: DateTime.fromISO('2021-06-23'),
      };

      this.set('itemData', itemData);
      this.set('isSelected', true);

      await render(hbs`<Statistics::MonthTimeTrackItem @data={{this.itemData}}
        @isSelected={{this.isSelected}} />`);

      assert
        .dom('[data-test-month-track-item]')
        .hasClass('selected', 'it is selected');

      this.set('isSelected', false);
      assert
        .dom('[data-test-month-track-item]')
        .hasNoClass('selected', 'it is unselected');
    });
  },
);
