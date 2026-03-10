// @ts-nocheck -- QUnit test context typing not supported with @types/qunit v2.9
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import ServerDown from 'brn/components/server-down';

module('Integration | Component | server-down', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders the server down page', async function (assert) {
    await render(<template><ServerDown /></template>);

    assert.dom('[data-test-server-down]').exists();
    assert.dom('[data-test-server-down-title]').hasText('t:server_down.title');
    assert.dom('[data-test-server-down-message]').hasText('t:server_down.message');
  });

  test('it shows the Telegram link with correct attributes', async function (assert) {
    await render(<template><ServerDown /></template>);

    const link = assert.dom('[data-test-server-down-telegram-link]');
    link.hasAttribute('href', 'https://t.me/BrainUpUsers');
    link.hasAttribute('target', '_blank');
    link.hasAttribute('rel', 'noopener noreferrer');
    link.hasText('https://t.me/BrainUpUsers');
  });

  test('it shows the fix promise message', async function (assert) {
    await render(<template><ServerDown /></template>);

    assert.dom('[data-test-server-down-fix-promise]').hasText('t:server_down.fix_promise');
  });
});
