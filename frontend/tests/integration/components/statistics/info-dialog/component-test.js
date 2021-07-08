import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import sinon from 'sinon';
import click from '@ember/test-helpers/dom/click';

module('Integration | Component | statistics/info-dialog', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });
    const closeModalAction = sinon.stub();
    this.set('closeModalAction', closeModalAction);

    await render(
      hbs`<Statistics::InfoDialog @closeModalAction={{this.closeModalAction}}/>`,
    );
    assert.dom('[data-test-info-dialog]').exists();
    await click('[data-test-button-ok]');
    assert.ok(closeModalAction.calledOnce);
    await click('[data-test-button-close]');
    assert.ok(closeModalAction.calledTwice);
  });
});
