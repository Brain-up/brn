import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { task } from 'ember-concurrency';

export default class LoginFormComponent extends Component {
	@service('session') session;
	@service('router') router;

	@tracked login = undefined;
	@tracked password = undefined;

	@tracked errorMessage = '';

	get usernameError() {
		if (this.login === undefined) {
			return false;
		}
		return (this.login || '').trim().length === 0;
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
      } catch(error) {
		if (error.responseJSON) {
			this.errorMessage = error.responseJSON.errors.pop();
		} else {
			this.errorMessage =  error.error || error;
		}
      }

      if (this.session.isAuthenticated) {
		this.router.transitionTo('index');
        // What to do with all this success?
      }
  }).drop()) loginTask;
	
	@action
	onSubmit(e) {
		e.preventDefault();
		this.loginTask.perform();
	}
}
