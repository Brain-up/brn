import LoginFormComponent from './../login-form/component';
import { action } from '@ember/object';
import { task } from 'ember-concurrency';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';

export default class RegistrationFormComponent extends LoginFormComponent {
  @service('network') network;
  @service('intl') intl;
  @tracked email;
  @tracked firstName;
  @tracked lastName;
  @tracked password;
  @tracked birthday;
  maxDate = new Date();
  minDate = new Date(new Date().setFullYear(this.maxDate.getFullYear() - 100));
  maxDateString = this.maxDate.toISOString().split('T')[0];
  minDateString = this.minDate.toISOString().split('T')[0];

  beforeModel() {
    this.intl.setLocale(['ru-ru']);
  }

  get warningErrorDate() {
    const { birthday, maxDate, minDate } = this;

    if (birthday === undefined) {
      return false;
    }

    const max = maxDate.getTime();
    const min = minDate.getTime();
    const enterDateUser = new Date(birthday).getTime();

    if (enterDateUser > max || min > enterDateUser) {
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
