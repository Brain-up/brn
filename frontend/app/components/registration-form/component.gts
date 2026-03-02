import LoginFormComponent from 'brn/components/login-form/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { task, Task } from 'ember-concurrency';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { getOwner } from '@ember/application';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import FirebaseAuthenticator from 'brn/authenticators/firebase';
import { isBornYearValid } from 'brn/utils/validators';
import { LinkTo } from '@ember/routing';
import { on } from '@ember/modifier';
import { t } from 'ember-intl';
import { eq } from 'ember-truth-helpers';
import { or } from 'ember-truth-helpers';
import LoadingSpinner from 'brn/components/loading-spinner/component';
import LoginFormInput from 'brn/components/login-form/input/component';
import UiButton from 'brn/components/ui/button';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const ERRORS_MAP = {
  'The user already exists!': 'registration_form.email_exists',
  PASSWORD_MUST_BE_BETWEEN_6_AND_20_CHARACTERS_LONG:
    'registration_form.password_length',
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars
interface LatestUserDTO {
  name: string;
  email: string;
  password: string;
  gender: 'MALE' | 'FEMALE';
  bornYear: number;
  avatar: string;
  id?: string;
}

export default class RegistrationFormComponent extends LoginFormComponent {
  @tracked email!: string;
  @tracked firstName!: string;
  @tracked lastName!: string;
  @tracked password!: string;
  @tracked birthday!: string;
  @tracked repeatPassword!: string;
  @tracked gender!: 'MALE' | 'FEMALE';
  @tracked agreed = false;

  @tracked agreedStatusErrorMessage = '';
  @tracked serverErrorMessage = '';

  get warningPasswordsEquality() {
    if (this.repeatPassword === undefined) {
      return false;
    }

    const isPasswordsEqual =
      this.password && this.password === this.repeatPassword;

    if (!isPasswordsEqual) {
      return this.intl.t('registration_form.passwords_should_match');
    }

    return false;
  }

  get warningErrorDate() {
    const { birthday } = this;

    if (birthday === undefined) {
      return false;
    }

    if (!isBornYearValid(birthday)) {
      return this.intl.t('registration_form.invalid_date');
    }

    return false;
  }

  get warningName() {
    if (this.firstName === undefined) {
      return false;
    }
    if (this.firstName.trim().split(' ').length === 1) {
      return this.intl.t('registration_form.empty_lastname');
    }
    return false;
  }

  get warningGender() {
    if (!this.birthday || !this.firstName) {
      return false;
    }
    if (!this.gender) {
      return this.intl.t('registration_form.empty_gender');
    }
    return false;
  }

  get registrationInProgress() {
    return (
      this.loginInProgress ||
      this.registrationTask.lastSuccessful ||
      this.registrationTask.isRunning
    );
  }
  // @ts-expect-error overrides property
  get login() {
    return this.email;
  }
  set login(value) {
    this.email = value;
  }
  @(task(function* (this: RegistrationFormComponent): Generator<unknown, void, any> {
    const user: LatestUserDTO = {
      name: this.firstName.trim(),
      email: this.email,
      gender: this.gender,
      avatar: '',
      bornYear: parseInt(this.birthday, 10),
      password: this.password,
    };
    try {
      const auth = getOwner(this).lookup(
        'authenticator:firebase',
      ) as FirebaseAuthenticator;
      yield auth.registerUser(user.email, user.password);
    } catch (e) {
      const error = e as Error;
      this.serverErrorMessage = error.message;
      yield this.registrationTask.cancelAll();
      return;
    }

    yield this.loginTask.perform();

    const result = yield this.network.patchUserInfo(user);
    if (result.ok) {
      return;
    } else {
      const error = yield result.json();
      const key = error.errors.pop();
      if (this.intl.exists(`msg.validation.${key}`)) {
        this.errorMessage = this.intl.t(`msg.validation.${key}`);
      } else {
        this.errorMessage =
          key in ERRORS_MAP
            ? this.intl.t(ERRORS_MAP[key as keyof typeof ERRORS_MAP])
            : key;
      }
      yield this.registrationTask.cancelAll();
    }
  }).drop())
  registrationTask!: Task<any, any>;

  @action
  onSubmit(e: SubmitEvent & any) {
    e.preventDefault();
    if (!this.agreed) {
      this.agreedStatusErrorMessage = this.intl.t('registration_form.agree_terms');
      return;
    }

    if (this.errorMessage) {
      return;
    }

    this.serverErrorMessage = ''

    this.registrationTask.perform();
  }

  @action
  setGender(e: SubmitEvent & any) {
    this.gender = e.target.value;
  }

  @action
  setAgreedStatus(e: Document & any) {
    this.agreed = e.target.checked;

    if (this.agreed) {
      this.agreedStatusErrorMessage = '';
    }
  }

  @action
  setBirthday(e: KeyboardEvent) {
    const key = e.key;
    const allowedKeys = [
      'Backspace',
      'Delete',
      'Tab',
      'Enter',
      'ArrowLeft',
      'ArrowRight',
    ];
    const isValid = allowedKeys.includes(key) || !isNaN(key as any);
    if (!isValid) {
      e.preventDefault();
    }

  }

  <template>
    <div class="w-full max-w-lg mx-auto">
      <form
        class="bg-white rounded sm:px-16 px-4 py-8 sm:py-16 mb-6
          {{unless this.registrationInProgress "shadow-lg"}}"
        {{on "submit" this.onSubmit}}
      >
        <div class="flex mb-4">
          <div
            class="w-1/2 text-sm font-bold tracking-wider text-center border-b-2 border-black"
          >
            {{t "registration_form.registration_hover"}}
          </div>
          <LinkTo
            @route="login"
            class="hover:text-blue-1100 inline-block w-1/2 pb-3 text-sm font-bold tracking-wider text-center text-gray-500 border-b-2"
          >
            {{t "registration_form.sign_in"}}
          </LinkTo>
        </div>
        {{#if this.registrationInProgress}}
          <LoadingSpinner />
        {{else}}
          <div class="mb-4">
            <LoginFormInput
              required
              @trimRight={{false}}
              @warning={{this.warningName}}
              @label={{t "registration_form.name"}}
              @model={{this}}
              @name="firstName"
              @placeholder={{t "registration_form.name_placeholder"}}
            />
          </div>
          <div class="mb-4">
            <LoginFormInput
              required
              minlength="4"
              maxlength="4"
              pattern="[0-9]{4}"
              @warning={{this.warningErrorDate}}
              @label={{t "registration_form.birthday"}}
              @placeholder={{t "registration_form.birthday_placeholder"}}
              @model={{this}}
              @name="birthday"
              {{on "keydown" this.setBirthday}}
            />
          </div>
          <div class="mb-4">
            <p class="mb-2 text-sm font-bold text-gray-700">
              {{t "registration_form.gender"}}
            </p>
            <input
              required
              name="gender"
              value="FEMALE"
              type="radio"
              class="w-3 h-3 border-gray-300"
              id="female"
              checked={{eq this.gender "FEMALE"}}
              {{on "change" this.setGender}}
            />
            <label class="ml-1 text-sm text-gray-500" for="female">
              {{t "registration_form.gender_female"}}
            </label>
            <input
              required
              name="gender"
              type="radio"
              value="MALE"
              class="w-3 h-3 ml-4"
              id="male"
              checked={{eq this.gender "MALE"}}
              {{on "change" this.setGender}}
            />
            <label class="ml-1 text-sm text-gray-500" for="male">
              {{t "registration_form.gender_male"}}
            </label>
    
            {{#if this.warningGender}}
              <p
                data-test-warning-message="gender"
                class="mt-2 text-xs text-red-500"
              >
                {{this.warningGender}}
              </p>
            {{/if}}
          </div>
          <div class="mb-4">
            <LoginFormInput
              @label={{t "registration_form.email"}}
              @placeholder={{t "registration_form.email_placeholder"}}
              @model={{this}}
              @name="email"
              @type="email"
            />
          </div>
          <div class="mb-4">
            <LoginFormInput
              @label={{t "registration_form.password"}}
              @placeholder={{t "registration_form.password_placeholder"}}
              @model={{this}}
              @name="password"
              @type="password"
            />
          </div>
          <div class="mb-6">
            <LoginFormInput
              @label={{t "registration_form.repeat_password"}}
              @placeholder={{t "registration_form.password_placeholder"}}
              @model={{this}}
              @warning={{this.warningPasswordsEquality}}
              @name="repeatPassword"
              @type="password"
            />
            {{#if (or this.usernameError this.passwordError)}}
              <p data-test-form-warning class="mt-2 text-xs text-red-500">
                {{t "registration_form.warning_enter_credentials"}}
              </p>
            {{/if}}
            {{#if this.errorMessage}}
              <p data-test-form-error class="mt-2 text-xs text-red-500">
                {{this.errorMessage}}
              </p>
            {{/if}}
            {{#if this.serverErrorMessage}}
              <p data-test-form-error class="mt-2 text-xs text-red-500">
                {{this.serverErrorMessage}}
              </p>
            {{/if}}
          </div>
          <div class="flex mb-4">
            <input
              id="agreement"
              name="agreement"
              type="checkbox"
              checked={{this.agreed}}
              {{on "change" this.setAgreedStatus}}
            />
            <label class="block ml-2 text-sm" for="agreement">
              {{t "registration_form.agreement_part1"}}
              <LinkTo
                @route="user-agreement"
                class="hover:text-indigo-600 text-indigo-500"
              >
                {{t "registration_form.agreement_part2"}}
              </LinkTo>
            </label>
          </div>
          {{#if this.agreedStatusErrorMessage}}
            <p data-test-form-error class="mt-2 text-xs text-red-500">
              {{this.agreedStatusErrorMessage}}
            </p>
          {{/if}}
          <div class="flex mb-4">
            <UiButton
              @type="submit"
              class="w-full"
              disabled={{false}}
              data-test-submit-form
              @title={{t "registration_form.registration"}}
            />
          </div>
        {{/if}}
      </form>
    </div>
  </template>
}
