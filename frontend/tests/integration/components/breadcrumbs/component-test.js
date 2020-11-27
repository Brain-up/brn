import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';
import { startRouting } from './test-support/helpers';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';

module('Integration | Component | breadcrumbs', function(hooks) {
  setupRenderingTest(hooks);
  setupMirage(hooks);

  hooks.beforeEach(function() {
    startRouting(this.owner);
  });

  test('it shows group link', async function(assert) {
    this.set('group', {
      name: 'group 1',
      id: 1,
      group_id: 1,
    });

    await render(hbs`<Breadcrumbs
      @group={{this.group}}
    />`);

    const groupLink = pageObject.groupLinks[0].anchor[0];
    assert.equal(groupLink.href, '/groups/1');
    assert.equal(groupLink.text, 'group 1');
    assert.notOk(pageObject.seriesLinkExists);
  });
});
