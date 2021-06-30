import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import { Service } from 'ember-intl';
import sinon from 'sinon';

module('Integration | Component | statistics', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });
    const stubGetStatsByYear = sinon.stub();
    const stubGetStatsByWeek = sinon.stub();

    const stubNetworkService = Service.extend({
      getUserStatisticsByYear(from, to) {
        stubGetStatsByYear.call(from, to);
        return [];
      },
      getUserStatisticsByWeek(from, to) {
        stubGetStatsByWeek.call(from, to);
        return [];
      },
    });
    this.owner.register('service:network', stubNetworkService);

    await render(hbs`<Statistics />`);

    assert.ok(
      stubGetStatsByWeek.calledOnce,
      'getUserStatisticsByWeek called on init',
    );
    assert.ok(
      stubGetStatsByYear.calledOnce,
      'getUserStatisticsByYear called on init',
    );
  });

  test('it shows info dialog', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });
    const stubNetworkService = Service.extend({
      getUserStatisticsByYear(from, to) {
        return [];
      },
      getUserStatisticsByWeek(from, to) {
        return [];
      },
    });
    this.owner.register('service:network', stubNetworkService);

    await render(hbs`<Statistics />`);

    assert.dom('[data-test-info-dialog]').doesNotExist();
    await click('[data-test-help-button]');
    assert.dom('[data-test-info-dialog]').exists();
    await click('[data-test-button-close]');
    assert.dom('[data-test-info-dialog]').doesNotExist();
  });
});
