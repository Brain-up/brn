import LoginFormComponent from './../login-form/component';
import fetch from 'fetch';
import { action } from '@ember/object';
import { task } from 'ember-concurrency';
import { tracked } from '@glimmer/tracking';
export default class RegistrationFormComponent extends LoginFormComponent {
    @tracked email;
    @tracked firstName;
    @tracked lastName;
    @tracked password;
    @tracked birthday;

    get login() {
        return this.email;
    }
    @(task(function*() {

        const user = {
            firstName: this.firstName,
            lastName: this.lastName,
            email: this.email,
            birthday: new Date(this.birthday).toISOString(),
            password: this.password,
        };
		const options = {
		  body: JSON.stringify(user),
		  headers: {
            'Content-Type':  'application/json'
          },
		  method: 'POST'
		};
        const result = yield fetch('api/registration', options);
        if (result.ok) {
            yield this.loginTask.perform();
        } else {
            const error = yield result.json();
            this.errorMessage = error.errors.pop();
        }
    }).drop()) registrationTask;

    @action
	onSubmit(e) {
		e.preventDefault();
		this.registrationTask.perform();
	}
}
