import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import StatisticsBarChart from 'brn/components/statistics/bar-chart';

module('Integration | Component | statistics/bar-chart', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    this.set('data', []);
    this.set('options', null);

    const self = this;




    await render(
      <template><StatisticsBarChart @data={{self.data}} @options={{self.options}} /></template>
    );

    assert.dom('#chart').exists();
  });
});
