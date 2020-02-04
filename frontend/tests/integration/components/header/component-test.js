import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';

module('Integration | Component | header', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    this.owner.lookup('router:main').setupRouter();
    await render(hbs`<Header />`);

    assert.equal(pageObject.groupsLink, '/groups', 'has a right groups link');
    assert.equal(pageObject.logoLink, '/', 'has core link in the main logo');
  });
});
