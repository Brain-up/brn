import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, click, fillIn, settled } from '@ember/test-helpers';
import { setupMSW } from '../../../helpers/msw';
import Service from '@ember/service';
import UiHeadphones from 'brn/components/ui/headphones';

module('Integration | Component | ui/headphones', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');
  setupMSW(hooks);

  test('shows loading skeleton while fetching headphones', async function (assert) {
    let resolveStore;
    class MockStore extends Service {
      findAll() {
        return new Promise((resolve) => {
          resolveStore = resolve;
        });
      }
    }
    this.owner.register('service:store', MockStore);

    render(<template><UiHeadphones /></template>);

    // Wait a tick for the component to enter loading state
    await new Promise((r) => setTimeout(r, 50));

    assert.dom('.animate-pulse').exists('loading skeleton is shown while loading');

    // Resolve the pending promise so it doesn't leak into other tests
    resolveStore([]);
    await settled();
  });

  test('shows empty state when no headphones exist', async function (assert) {
    server.get('users/current/headphones', () => ({ data: [] }));

    await render(<template><UiHeadphones /></template>);

    assert.dom('[data-test-headphone-item]').doesNotExist('no headphone items shown');
    assert.dom('[data-test-show-add-headphones]').exists('add button is visible');
  });

  test('renders headphone list with translated type labels', async function (assert) {
    const intl = this.owner.lookup('service:intl');

    server.get('users/current/headphones', () => ({
      data: [
        { id: '1', name: 'Sony XM5', active: true, type: 'OVER_EAR_BLUETOOTH', description: '', userAccount: '1' },
        { id: '2', name: 'AirPods Pro', active: true, type: 'IN_EAR_BLUETOOTH', description: '', userAccount: '1' },
      ],
    }));

    await render(<template><UiHeadphones /></template>);

    assert.dom('[data-test-headphone-item]').exists({ count: 2 }, 'two headphone items');

    const items = this.element.querySelectorAll('[data-test-headphone-item]');

    assert.dom(items[0].querySelector('[data-test-headphone-name]')).hasText('Sony XM5');
    assert.dom(items[0].querySelector('[data-test-headphone-type]')).hasText(
      intl.t('profile.headphones.types.OVER_EAR_BLUETOOTH'),
    );

    assert.dom(items[1].querySelector('[data-test-headphone-name]')).hasText('AirPods Pro');
    assert.dom(items[1].querySelector('[data-test-headphone-type]')).hasText(
      intl.t('profile.headphones.types.IN_EAR_BLUETOOTH'),
    );
  });

  test('toggling add form shows and hides the form', async function (assert) {
    server.get('users/current/headphones', () => ({ data: [] }));

    await render(<template><UiHeadphones /></template>);

    assert.dom('[data-test-add-headphones-form]').doesNotExist('form is hidden initially');

    await click('[data-test-show-add-headphones]');
    assert.dom('[data-test-add-headphones-form]').exists('form is shown after click');

    await click('[data-test-cancel-headphone]');
    assert.dom('[data-test-add-headphones-form]').doesNotExist('form is hidden after cancel');
  });

  test('shows validation error when submitting empty name', async function (assert) {
    const intl = this.owner.lookup('service:intl');

    server.get('users/current/headphones', () => ({ data: [] }));

    await render(<template><UiHeadphones /></template>);

    await click('[data-test-show-add-headphones]');
    await click('[data-test-submit-headphone]');

    assert.dom('[data-test-headphone-error]').exists('error message is shown');
    assert.dom('[data-test-headphone-error]').hasText(
      intl.t('profile.headphones.name_required'),
    );
  });

  test('successfully adds a headphone', async function (assert) {
    let postCalled = false;
    let postPayload = null;

    server.get('users/current/headphones', () => ({ data: [] }));
    server.post('users/current/headphones', (request) => {
      postCalled = true;
      postPayload = JSON.parse(request.requestBody);
      // After adding, the component reloads headphones
      server.get('users/current/headphones', () => ({
        data: [{ id: '1', name: postPayload.name, active: true, type: postPayload.type, description: '', userAccount: '1' }],
      }));
      return { data: { id: '1', name: postPayload.name, active: true, type: postPayload.type } };
    });

    await render(<template><UiHeadphones /></template>);

    await click('[data-test-show-add-headphones]');
    await fillIn('[data-test-headphone-name-input]', 'My New Headphones');
    await click('[data-test-submit-headphone]');

    assert.true(postCalled, 'POST was called');
    assert.strictEqual(postPayload.name, 'My New Headphones', 'correct name sent');
    assert.strictEqual(postPayload.type, 'NOT_DEFINED', 'default type sent');
    assert.strictEqual(postPayload.active, true, 'active flag sent');

    assert.dom('[data-test-add-headphones-form]').doesNotExist('form is hidden after submit');
    assert.dom('[data-test-headphone-item]').exists({ count: 1 }, 'new headphone appears in list');
  });

  test('delete flow shows confirm dialog and calls DELETE', async function (assert) {
    let deleteCalled = false;

    server.get('users/current/headphones', () => ({
      data: [
        { id: '42', name: 'Old Headphones', active: true, type: 'ON_EAR_NO_BLUETOOTH', description: '', userAccount: '1' },
      ],
    }));
    server.delete('users/current/headphones/:id', (request) => {
      deleteCalled = true;
      assert.strictEqual(request.params.id, '42', 'correct headphone ID');
      server.get('users/current/headphones', () => ({ data: [] }));
      return {};
    });

    await render(<template><UiHeadphones /></template>);

    assert.dom('[data-test-headphone-item]').exists({ count: 1 });

    await click('[data-test-delete-headphone]');
    assert.dom('[data-test-confirm-dialog]').exists('confirm dialog is shown');

    await click('[data-test-confirm-ok]');

    assert.true(deleteCalled, 'DELETE was called');
    assert.dom('[data-test-headphone-item]').doesNotExist('headphone removed from list');
  });

  test('cancel delete hides confirm dialog', async function (assert) {
    server.get('users/current/headphones', () => ({
      data: [
        { id: '1', name: 'Keep Me', active: true, type: 'NOT_DEFINED', description: '', userAccount: '1' },
      ],
    }));

    await render(<template><UiHeadphones /></template>);

    await click('[data-test-delete-headphone]');
    assert.dom('[data-test-confirm-dialog]').exists('confirm dialog is shown');

    await click('[data-test-confirm-cancel]');
    // eslint-disable-next-line ember/no-settled-after-test-helper -- native <dialog> close event fires asynchronously
    await settled();
    assert.dom('[data-test-confirm-dialog]').doesNotExist('confirm dialog is hidden');
    assert.dom('[data-test-headphone-item]').exists({ count: 1 }, 'headphone still in list');
  });

  test('headphone type select shows translated options', async function (assert) {
    const intl = this.owner.lookup('service:intl');

    server.get('users/current/headphones', () => ({ data: [] }));

    await render(<template><UiHeadphones /></template>);

    await click('[data-test-show-add-headphones]');

    const options = this.element.querySelectorAll('[data-test-headphone-type-select] option');
    assert.strictEqual(options.length, 7, 'all 7 headphone types available');
    assert.strictEqual(options[0].textContent.trim(), intl.t('profile.headphones.types.NOT_DEFINED'));
    assert.strictEqual(options[1].textContent.trim(), intl.t('profile.headphones.types.ON_EAR_BLUETOOTH'));
    assert.strictEqual(options[4].textContent.trim(), intl.t('profile.headphones.types.ON_EAR_NO_BLUETOOTH'));
  });

  test('has proper ARIA label', async function (assert) {
    const intl = this.owner.lookup('service:intl');

    server.get('users/current/headphones', () => ({ data: [] }));

    await render(<template><UiHeadphones /></template>);

    assert.dom('[role="region"]').hasAria('label', intl.t('profile.headphones.title'));
  });
});
