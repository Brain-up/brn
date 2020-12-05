import LoginFormComponent from './../login-form/component';
import { action } from '@ember/object';
import { task } from 'ember-concurrency';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';

const ERRORS_MAP = {
  'The user already exists!': 'registration_form.email_exists',
};

export default class RegistrationFormComponent extends LoginFormComponent {
  @service('network') network;
  @tracked email;
  @tracked firstName;
  @tracked lastName;
  @tracked password;
  @tracked birthday;
  maxDate = new Date().getFullYear();
  minDate = new Date().getFullYear() - 100;

  get warningErrorDate() {
    const { birthday, maxDate, minDate } = this;

    if (birthday === undefined) {
      return false;
    }

    if (birthday > maxDate || minDate > birthday) {
      return this.intl.t('registration_form.invalid_date');
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
      birthday: this.birthday,
      password: this.password,
    };
    const result = yield this.network.createUser(user);
    if (result.ok) {
      yield this.loginTask.perform();
    } else {
      const error = yield result.json();
      const key = error.errors.pop();
      if (this.intl.exists(`msg.validation.${key}`)) {
        this.errorMessage = this.intl.t(`msg.validation.${key}`);
      } else {
        this.errorMessage =
          key in ERRORS_MAP ? this.intl.t(ERRORS_MAP[key]) : key;
      }
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
