import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | header', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    this.owner.lookup('router:main').setupRouter();
    await render(hbs`<Header />`);

    assert.dom('[data-test-group-link]').hasAttribute('href', '/groups');
    assert.dom('[data-test-logo]').hasAttribute('href', '/');
  });
});
