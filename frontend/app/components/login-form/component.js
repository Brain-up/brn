import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { task } from 'ember-concurrency';

const ERRORS_MAP = {
  'Bad credentials': 'Неправильный логин или пароль.'
};

export default class LoginFormComponent extends Component {
  @service('session') session;
  @service('router') router;

  @tracked login = undefined;
  @tracked password = undefined;

  @tracked errorMessage = '';

  get loginInProgress() {
    return this.loginTask.lastSuccessful || this.loginTask.isRunning;
  }
  get usernameError() {
    if (this.login === undefined) {
      return false;
    }
    return (this.login || '').trim().length === 0 || (this.login || '').trim().indexOf('@') === -1;
  }
  get passwordError() {
    if (this.password === undefined) {
      return false;
    }
    return (this.password || '').trim().length === 0;
  }

  @(task(function*() {
    let { login, password } = this;
    try {
      yield this.session.authenticate('authenticator:oauth2', login, password);
    } catch (error) {
      let key = ''
      if (error.responseJSON) {
        key = error.responseJSON.errors.pop();
      } else {
        key = error.error || error;
      }
      this.errorMessage = ERRORS_MAP[key] || key;
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
    this.loginTask.perform();
  }
}
