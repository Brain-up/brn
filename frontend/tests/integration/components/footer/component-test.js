import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';

module('Integration | Component | footer', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    await render(hbs`<Footer />`);

    assert.equal(pageObject.logosCount, 2, 'has two logos');
    assert.equal(pageObject.supportMessageText, 'При поддержке');
  });
});
