import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Helper | subtract', function(hooks) {
  setupRenderingTest(hooks);

  test('it subtracts the second argument from the first', async function(assert) {
    this.set('minuend', 10);
    this.set('subtrahend', 2);

    await render(hbs`{{subtract this.minuend this.subtrahend}}`);

    assert.equal(this.element.textContent.trim(), '8');
  });
});
