import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import GlobalTimer from 'brn/components/global-timer';

module('Integration | Component | global-timer', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders with initial time 00:00', async function (assert) {
    await render(<template><GlobalTimer /></template>);
    assert.dom('[data-test-timer-container]').hasAttribute('title', '00 : 00');
  });

  test('timer container has px-3 mobile padding class', async function (assert) {
    await render(<template><GlobalTimer /></template>);
    assert.dom('[data-test-timer-container]').hasClass('px-3');
  });

  test('timer container has sm:px-5 desktop padding class', async function (assert) {
    await render(<template><GlobalTimer /></template>);
    assert.dom('[data-test-timer-container]').hasClass('sm:px-5');
  });

  test('timer container has rounded-full class', async function (assert) {
    await render(<template><GlobalTimer /></template>);
    assert.dom('[data-test-timer-container]').hasClass('rounded-full');
  });

  test('timer container has default pink color for 0 seconds', async function (assert) {
    await render(<template><GlobalTimer /></template>);
    assert.dom('[data-test-timer-container]').hasClass('bg-pink-secondary');
  });
});
