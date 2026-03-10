// @ts-nocheck -- QUnit test context typing not supported with @types/qunit v2.9
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import ErrorPage from 'brn/components/error-page';

module('Integration | Component | error-page', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders ServerDown for WarpDrive FetchError with status 500', async function (assert) {
    const model = { isRequestError: true, status: 500, code: 500 };

    await render(<template><ErrorPage @model={{model}} /></template>);

    assert.dom('[data-test-server-down]').exists('shows server-down page');
    assert.dom('pre').doesNotExist('does not show generic error');
  });

  test('it renders ServerDown for network error (status 0)', async function (assert) {
    const model = { isRequestError: true, status: 0, code: 0 };

    await render(<template><ErrorPage @model={{model}} /></template>);

    assert.dom('[data-test-server-down]').exists('shows server-down page for network error');
  });

  test('it renders ServerDown for status 503', async function (assert) {
    const model = { isRequestError: true, status: 503, code: 503 };

    await render(<template><ErrorPage @model={{model}} /></template>);

    assert.dom('[data-test-server-down]').exists('shows server-down page for 503');
  });

  test('it renders ServerDown for plain status 500 object', async function (assert) {
    const model = { status: 500 };

    await render(<template><ErrorPage @model={{model}} /></template>);

    assert.dom('[data-test-server-down]').exists('shows server-down page for plain 500');
  });

  test('it renders generic error for client errors (status 404)', async function (assert) {
    const model = { isRequestError: true, status: 404, code: 404 };

    await render(<template><ErrorPage @model={{model}} /></template>);

    assert.dom('[data-test-server-down]').doesNotExist('does not show server-down page');
    assert.dom('pre').exists('shows generic error pre block');
  });

  test('it renders generic error for string model', async function (assert) {
    const model = 'Something went wrong';

    await render(<template><ErrorPage @model={{model}} /></template>);

    assert.dom('[data-test-server-down]').doesNotExist('does not show server-down page');
    assert.dom('pre').hasText('Something went wrong');
  });

  test('it renders generic error for a regular Error object', async function (assert) {
    const model = new Error('Unknown failure');

    await render(<template><ErrorPage @model={{model}} /></template>);

    assert.dom('[data-test-server-down]').doesNotExist('does not show server-down page');
    assert.dom('pre').exists('shows generic error');
  });
});
