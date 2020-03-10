import LoginFormComponent from './../login-form/component';
import { action } from '@ember/object';
import { task } from 'ember-concurrency';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';

export default class RegistrationFormComponent extends LoginFormComponent {
  @service('network') network;
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
    const result = yield this.network.createUser(user);
    if (result.ok) {
      yield this.loginTask.perform();
    } else {
      const error = yield result.json();
      this.errorMessage = error.errors.pop();
    }
  }).drop())
  registrationTask;

  @action
  onSubmit(e) {
    e.preventDefault();
    this.registrationTask.perform();
  }
}
