import { module, test } from 'qunit';
import { click } from '@ember/test-helpers';
import { setupApplicationTest } from 'ember-qunit';
import { getServerResponses } from '../general-helpers';
import { getTestData } from './test-suport/data-storage';
import { authenticateSession } from 'ember-simple-auth/test-support';
import pageObject from './test-suport/page-object';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';

module('Acceptance | active seria highlighting test', function (hooks) {
  setupApplicationTest(hooks);
  setupMirage(hooks);

  hooks.beforeEach(async () => {
    await authenticateSession();
    getServerResponses(getTestData());
  });

  test('visiting /groups/1/series/1 has highligted link for active seria', async function (assert) {
    await pageObject.goToSeriesPage();

    assert.dom('[data-test-active-link="link-1"]').hasClass('active');
    assert.dom('[data-test-active-link="link-2"]').hasNoClass('active');

    await click('[data-test-active-link="link-2"]');

    assert.dom('[data-test-active-link="link-1"]').hasNoClass('active');
    assert.dom('[data-test-active-link="link-2"]').hasClass('active');
  });
});
