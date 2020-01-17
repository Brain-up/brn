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

  test('it shows series link if series object is passed', async function(assert) {
    this.set('group', {
      name: 'group 2',
      id: 2,
    });

    this.set('series', {
      name: 'series 1',
      id: 1,
    });

    await render(hbs`<Breadcrumbs
      @series={{this.series}}
      @group={{this.group}}
    />`);

    const groupLink = pageObject.groupLinks[0].anchor[0];
    const seriesLink = pageObject.seriesLinks[0].anchor[0];
    assert.equal(groupLink.href, '/groups/2');
    assert.equal(groupLink.text, 'group 2');
    assert.equal(seriesLink.href, '/groups/2/series/1');
    assert.equal(seriesLink.text, 'series 1');
  });
});
