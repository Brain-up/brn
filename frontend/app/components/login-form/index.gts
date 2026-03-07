import Component from '@glimmer/component';
import { service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { dropTask, timeout } from 'ember-concurrency';
import Router from '@ember/routing/router-service';
import Session from 'ember-simple-auth/services/session';
import IntlService from 'ember-intl/services/intl';
import NetworkService from 'brn/services/network';
import { LinkTo } from '@ember/routing';
import { on } from '@ember/modifier';
import { t } from 'ember-intl';
import { or } from 'ember-truth-helpers';
import { hash } from '@ember/helper';
import { get } from '@ember/helper';
import LoadingSpinner from 'brn/components/loading-spinner';
import LoginFormInput from 'brn/components/login-form/input';
import UiButton from 'brn/components/ui/button';

const BUTTON_STATES = {
  ACTIVE: 'active',
  DISABLED: 'disabled',
};

export default class LoginFormComponent extends Component {
  @service('session') session!: Session;
  @service('router') router!: Router;
  @service('network') network!: NetworkService;
  @service('intl') intl!: IntlService;

  @tracked login: string | undefined = undefined;
  @tracked password: string | undefined = undefined;

  @tracked errorMessage = '';

  get loginInProgress() {
    return this.loginTask.lastSuccessful || this.loginTask.isRunning;
  }

  get buttonState() {
    if (this.loginInProgress) {
      return BUTTON_STATES.DISABLED;
    }
    if (this.usernameError || this.passwordError) {
      return BUTTON_STATES.DISABLED;
    }
    return BUTTON_STATES.ACTIVE;
  }

  get usernameError() {
    const { login } = this;
    if (login === undefined) {
      return false;
    }
    const trimmedLogin = this.trimmedValue(login);
    return trimmedLogin.length === 0 || trimmedLogin.indexOf('@') === -1;
  }
  get passwordError() {
    if (this.password === undefined) {
      return false;
    }
    return this.trimmedValue(this.password).length === 0;
  }

  trimmedValue(value: string) {
    return (value || '').trim();
  }

  loginTask = dropTask(async () => {
    const { login, password } = this;
    try {
      await this.session.authenticate(
        'authenticator:firebase',
        login,
        password,
      );
      await timeout(500);
      await this.network.loadCurrentUser();
    } catch (error) {
      let key = '';
      if (error.responseJSON) {
        key = error.responseJSON.errors.pop();
      } else {
        key = error.error || error;
      }

      if (this.intl.exists(`msg.validation.${key}`)) {
        this.errorMessage = this.intl.t(`msg.validation.${key}`);
      } else {
        this.errorMessage = key;
      }

      await this.loginTask.cancelAll();
    }

    if (this.session.isAuthenticated) {
      this.router.transitionTo('index');
      // What to do with all this success?
    }
  });

  @action
  onSubmit(e: Event) {
    e.preventDefault();
    e.stopPropagation();
    if (this.buttonState === BUTTON_STATES.DISABLED) {
      return;
    }
    this.loginTask.perform();
  }

  <template>
    <form
      class="rounded-xl sm:px-16 sm:py-16 px-4 py-8 mb-6 bg-white shadow-lg" {{on "submit" this.onSubmit}}
    >
      <div class="relative z-10 bg-white rounded-lg">
        {{#if this.loginInProgress}}
          <LoadingSpinner />
        {{else}}
          <div class="flex mb-8">
            <LinkTo
              @route="registration" class="hover:text-blue-1100 inline-block w-1/2 pb-3 text-sm font-bold tracking-wider text-center text-gray-500 border-b-2"
            >
              {{t "registration_form.registration_hover"}}
    
            </LinkTo>
            <div
              class="w-1/2 text-sm font-bold tracking-wider text-center border-b-2 border-black"
            >
              {{t "registration_form.sign_in"}}
            </div>
          </div>
    
          <div class="mb-4">
            <LoginFormInput
              @placeholder={{t "login_form.login"}}
              @model={{this}}
              @name="login"
            />
          </div>
          <div class="mb-3">
            <LoginFormInput
              autocomplete="current-password"
              @placeholder={{t "login_form.password"}}
              @model={{this}}
              @name="password"
              @type="password"
            />
            {{#if (or this.usernameError this.passwordError)}}
              <p data-test-form-warning class="text-xs italic text-red-500">
                {{t "login_form.warning_enter_credentials"}}
              </p>
            {{/if}}
            {{#if this.errorMessage}}
              <p data-test-form-error class="mt-2 text-xs italic text-red-500">
                {{this.errorMessage}}
              </p>
            {{/if}}
          </div>
          <div class="mb-8 text-sm leading-5 text-right">
            <LinkTo
              @route="password-recovery" class="hover:text-indigo-600 text-md focus:outline-hidden focus:underline font-medium text-indigo-500 transition duration-150 ease-in-out"
            >
              {{t "login_form.forgot_password"}}
            </LinkTo>
          </div>
          <div class="flex mb-4">
            {{#let
              (hash
                active="bg-blue-700 hover:bg-blue-900 focus:outline-hidden focus:ring-2 focus:ring-blue-500/50"
                disabled="bg-blue-700 opacity-50  cursor-not-allowed"
              )
              as |buttonState|
            }}
              <UiButton
                @size="small"
                data-test-submit-form
                class="{{get buttonState this.buttonState}} w-full"
                @title={{t "login_form.sign_in"}}
              />
            {{/let}}
          </div>
        {{/if}}
      </div>
    </form>
  </template>
}
