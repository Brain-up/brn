import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import waitFor from '@ember/test-helpers/dom/wait-for';

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

    await waitFor('#chart');
    assert.dom('#chart').exists();
  });
});
