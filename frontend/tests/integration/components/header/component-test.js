import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import {
  authenticateSession,
  invalidateSession,
} from 'ember-simple-auth/test-support';

module('Integration | Component | header', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    this.owner.setupRouter();
    await authenticateSession();
    await render(hbs`<Header />`);

    assert
      .dom('[data-test-group-link]')
      .hasAttribute('href', /^\/groups\?locale=(?:en-us|ru-ru)$/);
    assert.dom('[data-test-logo]').hasAttribute('href', '/');
  });

  test('it does not render group link if user is not authorized', async function (assert) {
    this.owner.setupRouter();
    await invalidateSession();
    await render(hbs`<Header />`);

    assert.dom('[data-test-group-link]').doesNotExist();
    assert.dom('[data-test-logo]').hasAttribute('href', '/');
  });
});
