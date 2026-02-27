import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | start-task-button', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    assert.expect(1);

    this.set('startTaskAction', function () {
      assert.ok(true, 'calls startTask action');
    });

    await render(hbs`<StartTaskButton @startTask={{this.startTaskAction}}/>`);

    await click('[data-test-start-task-button]');
  });
});
