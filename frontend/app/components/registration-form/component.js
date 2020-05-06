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

  get minYearString() {
    const now = new Date();
    now.setFullYear(now.getFullYear() - 100);
    return now.toISOString().split('T')[0];
  }

  get maxYearString() {
    const date = new Date();
    return date.toISOString().split('T')[0];
  }

  get warningErrorDate() {
    const { birthday } = this;
    if (birthday === undefined) {
      return false;
    }
    let now = new Date();
    let maxString = now.toISOString().split('T')[0];
    now.setFullYear(now.getFullYear() - 100);
    let minString = now.toISOString().split('T')[0];
    let maxNumberValid = parseInt(maxString.replace(/-/g, ''));
    let enterUserValue = parseInt(birthday.replace(/-/g, ''));
    let minNumberValid = parseInt(minString.replace(/-/g, ''));
    if (maxNumberValid < enterUserValue || minNumberValid > enterUserValue) {
      return 'Некорректная дата';
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
      this.registrationTask.cancelAll();
    }
  }).drop())
  registrationTask;

  @action
  onSubmit(e) {
    e.preventDefault();
    this.registrationTask.perform();
  }
}
