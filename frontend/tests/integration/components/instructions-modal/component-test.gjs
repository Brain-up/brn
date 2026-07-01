import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import InstructionsModal from 'brn/components/instructions-modal';

// ember-intl returns the `t:<key>` placeholder in this test env (see
// doctor-feedback/component-test.gjs for the same pattern). Assertions
// target the key bindings, not translated strings.

module('Integration | Component | instructions-modal', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('the trigger is rendered but the dialog stays closed by default', async function (assert) {
    await render(<template><InstructionsModal /></template>);

    assert.dom('[data-test-instructions-trigger]').exists();
    assert.dom('[data-test-instructions-trigger]').hasAttribute('aria-expanded', 'false');
    assert.dom('[data-test-instructions-dialog]').doesNotExist();
  });

  test('clicking the trigger opens the dialog with the four-step content', async function (assert) {
    await render(<template><InstructionsModal /></template>);

    await click('[data-test-instructions-trigger]');

    assert.dom('[data-test-instructions-dialog]').exists('dialog is rendered');
    assert
      .dom('[data-test-instructions-trigger]')
      .hasAttribute('aria-expanded', 'true');
    assert
      .dom('[data-test-instructions-dialog-title]')
      .hasText('t:instructions.title');
    assert.dom('[data-test-instructions-intro]').exists();
    assert.dom('[data-test-instructions-dialog] ol > li').exists({ count: 4 });
  });

  test('clicking the footer close button closes the dialog', async function (assert) {
    await render(<template><InstructionsModal /></template>);

    await click('[data-test-instructions-trigger]');
    assert.dom('[data-test-instructions-dialog]').exists();

    await click('[data-test-instructions-dialog-close]');

    assert.dom('[data-test-instructions-dialog]').doesNotExist();
    assert
      .dom('[data-test-instructions-trigger]')
      .hasAttribute('aria-expanded', 'false');
  });

  test('clicking the header X button closes the dialog', async function (assert) {
    await render(<template><InstructionsModal /></template>);

    await click('[data-test-instructions-trigger]');
    await click('[data-test-instructions-dialog-close-x]');

    assert.dom('[data-test-instructions-dialog]').doesNotExist();
  });

  test('the trigger has aria metadata for assistive tech', async function (assert) {
    await render(<template><InstructionsModal /></template>);

    const trigger = '[data-test-instructions-trigger]';
    assert.dom(trigger).hasAttribute('aria-haspopup', 'dialog');
    assert.dom(trigger).hasAttribute('aria-controls', 'instructions-dialog-title');
    assert.dom(trigger).hasAttribute('aria-label', 't:instructions.trigger_aria');
  });
});
