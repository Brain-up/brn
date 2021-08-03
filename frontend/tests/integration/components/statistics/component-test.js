import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import sinon from 'sinon';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';
import { DateTime } from 'luxon';

module('Integration | Component | statistics', function (hooks) {
  setupRenderingTest(hooks);
  setupMirage(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });
    const stubGetStatsByYear = sinon.stub();
    const stubGetStatsByWeek = sinon.stub();

    server.get('/statistics/study/week', function (schema, request) {
      stubGetStatsByWeek(request.queryParams.from, request.queryParams.to);
      return { data: [] };
    });

    server.get('/statistics/study/year', function (schema, request) {
      stubGetStatsByYear(request.queryParams.from, request.queryParams.to);
      return { data: [] };
    });

    this.set(
      'initialSelectedMonth',
      DateTime.fromFormat('2021-01-20', 'yyyy-MM-dd', { zone: 'utc' }),
    );

    await render(
      hbs`<Statistics @initialSelectedMonth={{this.initialSelectedMonth}}/>`,
    );

    assert.ok(
      stubGetStatsByWeek.calledOnce,
      'getUserStatisticsByWeek called on init',
    );
    assert.ok(stubGetStatsByWeek.calledWith('2021-01-01', '2021-01-31'));
    assert.ok(
      stubGetStatsByYear.calledOnce,
      'getUserStatisticsByYear called on init',
    );
    assert.ok(stubGetStatsByYear.calledWith('2021-01-01', '2021-12-31'));
  });

  test('it shows info dialog', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    server.get('/statistics/study/week', function () {
      return { data: [] };
    });

    server.get('/statistics/study/year', function () {
      return { data: [] };
    });

    await render(hbs`<Statistics />`);

    assert.dom('[data-test-info-dialog]').doesNotExist();
    await click('[data-test-help-button]');
    assert.dom('[data-test-info-dialog]').exists();
    await click('[data-test-button-close]');
    assert.dom('[data-test-info-dialog]').doesNotExist();
  });
});
