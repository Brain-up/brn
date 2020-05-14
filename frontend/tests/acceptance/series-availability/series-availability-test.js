import { module, test } from 'qunit';
import { click } from '@ember/test-helpers';
import { setupApplicationTest } from 'ember-qunit';
import { getServerResponses } from '../general-helpers';
import { getTestData } from './test-suport/data-storage';
import { authenticateSession } from 'ember-simple-auth/test-support';
import pageObject from './test-suport/page-object';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';

module('Acceptance | series availability/series availability test', function(
  hooks,
) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  hooks.beforeEach(async function() {
    await authenticateSession();
    getServerResponses(getTestData());
  });

  test('visiting /series-availability/if link is active add class font-bolder', async function(assert) {
    await pageObject.goToSeriesPage();

    assert.dom('[data-test-active-link="link-1"]').hasClass('font-bold');
    assert.dom('[data-test-active-link="link-2"]').haNoClass('font-bold');

    await click('[data-test-active-link="link-2"]');

    assert.dom('[data-test-active-link="link-1"]').hasNoClass('font-bold');
    assert.dom('[data-test-active-link="link-2"]').hasClass('font-bold');
  });
});
