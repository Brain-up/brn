import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | statistics/bar-chart', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    this.set('data', []);
    this.set('options', null);

    await render(
      hbs`<Statistics::BarChart @data={{this.data}} @options={{this.options}} />`,
    );

    assert.dom('#chart').exists();
  });
});
