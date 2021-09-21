import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { Task, task } from 'ember-concurrency';
import Router from '@ember/routing/router-service';
import Session from 'ember-simple-auth/services/session';
import IntlService from 'ember-intl/services/intl';
import NetworkService from 'brn/services/network';

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

  @(task(function* (this: LoginFormComponent) {
    const { login, password } = this;
    try {
      yield this.session.authenticate('authenticator:firebase', login, password);
      yield this.network.loadCurrentUser();
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

      this.loginTask.cancelAll();
    }

    if (this.session.isAuthenticated) {
      this.router.transitionTo('index');
      // What to do with all this success?
    }
  }).drop())
  loginTask!: Task<any, any>;

  @action
  onSubmit(e: Event) {
    e.preventDefault();
    e.stopPropagation();
    if (this.buttonState === BUTTON_STATES.DISABLED) {
      return;
    }
    this.loginTask.perform();
  }
}
