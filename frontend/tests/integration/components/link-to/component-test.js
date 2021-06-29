import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';

module('Integration | Component | link-to', function (hooks) {
  setupRenderingTest(hooks);

  test('it doesnt have aria-current if not active', async function (assert) {
    await render(hbs`
    {{#link-to "series"}}
        text
      {{/link-to}}
    `);

    assert.notOk(pageObject.hasAriaCurrentAttribute);
  });

  test('it has aria-current attribute if active', async function (assert) {
    await render(hbs`
   {{#link-to "series"
    active=true}}
        text
      {{/link-to}}
    `);
    assert.ok(pageObject.hasAriaCurrentAttribute);
  });
});
