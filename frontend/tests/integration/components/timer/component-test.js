import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import mockService from '../../../test-support/mock-service';

module('Integration | Component | timer', function (hooks) {
  setupRenderingTest(hooks);

  test('supports mm:ss format', async function (assert) {
    mockService(this.owner, 'studying-timer', {
      countedSeconds: 67,
      pause() {},
      register() {},
      unregister() {},
      togglePause() {},
    });
    await render(hbs`<Timer />`);

    assert.dom('[data-test-timer-display-value]').hasText('01:07');
  });

  test('supports hh:mm:ss format', async function (assert) {
    mockService(this.owner, 'studying-timer', {
      countedSeconds: 3705,
      pause() {},
      register() {},
      unregister() {},
      togglePause() {},
    });
    await render(hbs`<Timer />`);

    assert.dom('[data-test-timer-display-value]').hasText('01:01:45');
  });

  test('continues with time from studying-timer', async function (assert) {
    mockService(this.owner, 'studying-timer', {
      countedSeconds: 94,
      pause() {},
      register() {},
      unregister() {},
      togglePause() {},
    });
    await render(hbs`<Timer @paused={{true}}/>`);

    assert.dom('[data-test-timer-display-value]').hasText('01:34');
  });

  test('pauses on idle', async function (assert) {
    mockService(this.owner, 'studying-timer', {
      countedSeconds: 3705,
      pause() {},
      isPaused: true,
      register() {},
      unregister() {},
      togglePause() {},
    });
    await render(hbs`<Timer />`);

    assert
      .dom('[data-test-timer-wrapper]')
      .hasAttribute('data-test-timer-is-paused');
  });
});
