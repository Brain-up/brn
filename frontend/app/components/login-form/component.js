import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { task } from 'ember-concurrency';

const ERRORS_MAP = {
  'Bad credentials': 'login_form.warning_enter_credentials',
};

const BUTTON_STATES = {
  ACTIVE: 'active',
  DISABLED: 'disabled',
};

export default class LoginFormComponent extends Component {
  @service('session') session;
  @service('router') router;
  @service('intl') intl;

  @tracked login = undefined;
  @tracked password = undefined;

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

  trimmedValue(value) {
    return (value || '').trim();
  }

  @(task(function*() {
    let { login, password } = this;
    try {
      yield this.session.authenticate('authenticator:oauth2', login, password);
    } catch (error) {
      let key = '';
      if (error.responseJSON) {
        key = error.responseJSON.errors.pop();
      } else {
        key = error.error || error;
      }

      this.errorMessage =
        key in ERRORS_MAP ? this.intl.t(ERRORS_MAP[key]) : key;

      this.loginTask.cancelAll();
    }

    if (this.session.isAuthenticated) {
      this.router.transitionTo('index');
      // What to do with all this success?
    }
  }).drop())
  loginTask;

  @action
  onSubmit(e) {
    e.preventDefault();
    if (this.buttonState === BUTTON_STATES.DISABLED) {
      return;
    }
    this.loginTask.perform();
  }
}
