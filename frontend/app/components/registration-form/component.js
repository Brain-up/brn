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

  getCurrentDateAndFormat(dateTime) {
    let today = dateTime;
    let dd = today.getDate();
    let MM = today.getMonth() + 1;
    let yyyy = today.getFullYear();

    if (dd < 10) {
      dd = '0' + dd;
    }

    if (MM < 10) {
      MM = '0' + MM;
    }

    today = yyyy + '-' + MM + '-' + dd;

    return today;
  }
  

  get getMinYear() {
    let today = new Date();
    today.setDate(today.getDate() - 365 * 100 - (100 / 4));
    let minYear = this.getCurrentDateAndFormat(today);
    
    return minYear;
  }

  get getMaxYear() {
    let today = new Date();
    let maxYear = this.getCurrentDateAndFormat(today);
    
    return maxYear;
  }

  get registrationInProgress() {
    return (
      this.loginInProgress ||
      this.registrationTask.lastSuccessful ||
      this.registrationTask.isRunning
    );
  }



  get value() {
    const { model, name } = this.args;
    if (!model) {
      return undefined;
    }
    return model[name];
  }

  set value(value) {
    const { model, name } = this.args;
    model[name] = (value||'').trim().slice(0, this.maxlength - 1);
  }

  helper() {
    this.getVal;
  }

  get getVal() {
    let today = new Date();
    let inputValue = birthday.value;
    today = this.getCurrentDateAndFormat(today);

    let maxValue = +today.replace(/-/g, "");
    let userValue = +inputValue.replace(/-/g, "");
    if (userValue <= maxValue ) {
      return

    } else {
      birthday.value = ""
      birthday.value = today;
      console.log(userValue)
    }
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
      date: this.date,
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

  @action getValue() {
    this.helper()
  }
  
  onSubmit(e) {
    e.preventDefault();
    this.registrationTask.perform();
  }
}
