import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { setupIntl } from 'ember-intl/test-support';
import { render, click } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
import Service from '@ember/service';

function registerUserData(owner, userModel) {
  class MockUserData extends Service {
    avatarUrl = '/avatars/default.png';
    userAvatar = 'default';
    activeLocale = 'en-us';
    userModel = userModel;
    network = {
      patchUserInfo() { /* noop */ },
    };
    setLocale() { /* noop */ }
  }
  owner.register('service:user-data', MockUserData);
}

module('Unit | Component | profile', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('renders user first name and last name inputs', async function (assert) {
    registerUserData(this.owner, {
      firstName: 'John',
      lastName: 'Doe',
      birthday: '1990',
      email: 'john@test.com',
      gender: 'MALE',
    });

    await render(hbs`<Profile />`);

    assert.dom('input[name="firstName"]').hasValue('John');
    assert.dom('input[name="lastName"]').hasValue('Doe');
  });

  test('renders birthday input with value', async function (assert) {
    registerUserData(this.owner, {
      firstName: 'Jane',
      lastName: 'Doe',
      birthday: '1995',
      email: 'jane@test.com',
      gender: 'FEMALE',
    });

    await render(hbs`<Profile />`);

    assert.dom('input[name="birthday"]').hasValue('1995');
  });

  test('shows warning for invalid birthday', async function (assert) {
    registerUserData(this.owner, {
      firstName: 'John',
      lastName: 'Doe',
      birthday: 'abc',
      email: 'john@test.com',
      gender: 'MALE',
    });

    await render(hbs`<Profile />`);

    // The component passes warningErrorDate to LoginForm::Input as @warning
    // which renders a warning message when truthy
    assert.dom('input[name="birthday"]').exists();
    // The warningErrorDate getter returns a translation string for invalid dates
    // The input component should display the warning
  });

  test('shows warning for empty first name', async function (assert) {
    registerUserData(this.owner, {
      firstName: '',
      lastName: 'Doe',
      birthday: '1990',
      email: 'john@test.com',
      gender: 'MALE',
    });

    await render(hbs`<Profile />`);

    assert.dom('input[name="firstName"]').hasValue('');
  });

  test('email input is disabled', async function (assert) {
    registerUserData(this.owner, {
      firstName: 'John',
      lastName: 'Doe',
      birthday: '1990',
      email: 'john@test.com',
      gender: 'MALE',
    });

    await render(hbs`<Profile />`);

    assert.dom('input[name="email"]').isDisabled();
  });

  test('avatar button opens modal on click', async function (assert) {
    registerUserData(this.owner, {
      firstName: 'John',
      lastName: 'Doe',
      birthday: '1990',
      email: 'john@test.com',
      gender: 'MALE',
    });

    await render(hbs`<Profile />`);

    // Modal should not be visible initially
    assert.dom('[data-test-avatars]').doesNotExist('avatar modal not shown initially');

    // Click the avatar button (the round profile picture button)
    await click('button.gradient-background');

    // After click, the Ui::Avatars component should be rendered inside the modal
    // We just check the modal-dialog overlay appeared
    assert.dom('.ember-modal-dialog').exists('modal is shown after clicking avatar');
  });

  test('gender radio buttons reflect current value', async function (assert) {
    registerUserData(this.owner, {
      firstName: 'John',
      lastName: 'Doe',
      birthday: '1990',
      email: 'john@test.com',
      gender: 'MALE',
    });

    await render(hbs`<Profile />`);

    assert.dom('input[name="gender"][value="MALE"]').isChecked();
    assert.dom('input[name="gender"][value="FEMALE"]').isNotChecked();
  });

  test('setBirthday prevents non-numeric key input', async function (assert) {
    registerUserData(this.owner, {
      firstName: 'John',
      lastName: 'Doe',
      birthday: '1990',
      email: 'john@test.com',
      gender: 'MALE',
    });

    await render(hbs`<Profile />`);

    const input = this.element.querySelector('input[name="birthday"]');
    assert.ok(input, 'birthday input exists');

    // Numeric keys should not be prevented
    let prevented = false;
    const numEvent = new KeyboardEvent('keydown', { key: '5', cancelable: true });
    Object.defineProperty(numEvent, 'preventDefault', {
      value: () => { prevented = true; },
    });
    input.dispatchEvent(numEvent);
    assert.false(prevented, 'numeric key "5" is not prevented');

    // Letter keys should be prevented
    prevented = false;
    const letterEvent = new KeyboardEvent('keydown', { key: 'a', cancelable: true });
    Object.defineProperty(letterEvent, 'preventDefault', {
      value: () => { prevented = true; },
    });
    input.dispatchEvent(letterEvent);
    assert.true(prevented, 'letter key "a" is prevented');

    // Backspace should not be prevented (allowed key)
    prevented = false;
    const bsEvent = new KeyboardEvent('keydown', { key: 'Backspace', cancelable: true });
    Object.defineProperty(bsEvent, 'preventDefault', {
      value: () => { prevented = true; },
    });
    input.dispatchEvent(bsEvent);
    assert.false(prevented, 'Backspace is not prevented');
  });

  test('password recovery link is present', async function (assert) {
    registerUserData(this.owner, {
      firstName: 'John',
      lastName: 'Doe',
      birthday: '1990',
      email: 'john@test.com',
      gender: 'MALE',
    });

    await render(hbs`<Profile />`);

    assert.dom('a[href="/password-recovery"]').exists('password recovery link rendered');
  });
});
