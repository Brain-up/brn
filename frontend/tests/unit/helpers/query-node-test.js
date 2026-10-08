import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Unit | Helper | query-node', function (hooks) {
  setupRenderingTest(hooks);

  test('returns a DOM element matching the selector', async function (assert) {
    await render(hbs`
      <div id="qn-target">hello</div>
      <span data-test>{{if (query-node "#qn-target") "found" "not found"}}</span>
    `);
    assert.dom('[data-test]').hasText('found');
  });

  test('returns null for a non-matching selector', async function (assert) {
    await render(hbs`
      <span data-test>{{if (query-node "#does-not-exist") "found" "not found"}}</span>
    `);
    assert.dom('[data-test]').hasText('not found');
  });
});
