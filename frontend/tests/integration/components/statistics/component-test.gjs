import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import sinon from 'sinon';
import { setupMSW } from '../../../helpers/msw';
import { DateTime } from 'luxon';
import Statistics from 'brn/components/statistics';

module('Integration | Component | statistics', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');
  setupMSW(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });
    const stubGetStatsByYear = sinon.stub();
    const stubGetStatsByWeek = sinon.stub();

    server.get('/v2/statistics/study/week', function (schema, request) {
      stubGetStatsByWeek(request.queryParams.from, request.queryParams.to);
      return { data: [] };
    });

    server.get('/v2/statistics/study/year', function (schema, request) {
      stubGetStatsByYear(request.queryParams.from, request.queryParams.to);
      return { data: [] };
    });

    this.set(
      'initialSelectedMonth',
      DateTime.fromFormat('2021-01-20', 'yyyy-MM-dd', { zone: 'utc' }),
    );

    const self = this;




    await render(
      <template><Statistics @initialSelectedMonth={{self.initialSelectedMonth}} /></template>
    );

    assert.ok(
      stubGetStatsByWeek.calledOnce,
      'getUserStatisticsByWeek called on init',
    );
    assert.ok(stubGetStatsByWeek.calledWith('2021-01-01T00:00:00', '2021-01-31T23:59:59'));
    assert.ok(
      stubGetStatsByYear.calledOnce,
      'getUserStatisticsByYear called on init',
    );
    assert.ok(stubGetStatsByYear.calledWith('2021-01-01T00:00:00', '2021-12-31T23:59:59'));
  });

  test('it shows info dialog', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    server.get('/v2/statistics/study/week', function () {
      return { data: [] };
    });

    server.get('/v2/statistics/study/year', function () {
      return { data: [] };
    });

    await render(<template><Statistics /></template>);

    assert.dom('[data-test-info-dialog]').doesNotExist();
    await click('[data-test-help-button]');
    assert.dom('[data-test-info-dialog]').exists();
    await click('[data-test-button-close]');
    assert.dom('[data-test-info-dialog]').doesNotExist();
  });
});
