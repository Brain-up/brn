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

    assert.dom('[data-test-group-link]').hasAttribute('href', '/groups');
    assert.dom('[data-test-logo]').hasAttribute('href', '/');
  });

  test('it not render group link if user not authorized', async function (assert) {
    this.owner.setupRouter();
    await invalidateSession();
    await render(hbs`<Header />`);

    assert.dom('[data-test-group-link]').doesNotExist();
    assert.dom('[data-test-logo]').hasAttribute('href', '/');
  });
});
