import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | link-to', function(hooks) {
  setupRenderingTest(hooks);

  test('it doesnt have aria-current attribute by default', async function(assert) {
    await render(hbs`
	  {{#link-to "series" 
		active=true}}
        text
      {{/link-to}}
    `);

    assert.dom('[aria-current]').doesNotExist();
  });

  test('it has aria-current attribute if it has putActiveAttr as true', async function(assert) {
    await render(hbs`
	  {{#link-to "series" 
	  	putActiveAttr=true
		active=true}}
        text
      {{/link-to}}
    `);
    assert.dom('[aria-current]').exists();
  });
});
