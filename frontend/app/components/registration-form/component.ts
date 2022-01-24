import LoginFormComponent from 'brn/components/login-form/component';
import { action } from '@ember/object';
import { task, Task } from 'ember-concurrency';
import { tracked } from '@glimmer/tracking';
import { getOwner } from '@ember/application';
import FirebaseAuthenticator from 'brn/authenticators/firebase';

const ERRORS_MAP = {
  'The user already exists!': 'registration_form.email_exists',
  PASSWORD_MUST_BE_BETWEEN_6_AND_20_CHARACTERS_LONG:
    'registration_form.password_length',
};

interface LatestUserDTO {
  name: string;
  email: string;
  password: string;
  gender: 'MALE' | 'FEMALE';
  bornYear: number;
  avatar: string;
  id?: string;
}

export default class RegistrationFormComponent extends LoginFormComponent {
  @tracked email!: string;
  @tracked firstName!: string;
  @tracked lastName!: string;
  @tracked password!: string;
  @tracked birthday!: string;
  @tracked repeatPassword!: string;
  @tracked gender!: 'MALE' | 'FEMALE';
  @tracked agreed = false;

  maxDate = new Date().getFullYear();
  minDate = new Date().getFullYear() - 100;

  get warningPasswordsEquality() {
    if (this.repeatPassword === undefined) {
      return false;
    }

    const isPasswordsEqual =
      this.password && this.password === this.repeatPassword;

    if (!isPasswordsEqual) {
      return this.intl.t('registration_form.passwords_should_match');
    }

    return false;
  }

  get warningErrorDate() {
    const { birthday, maxDate, minDate } = this;

    if (birthday === undefined) {
      return false;
    }

    if (parseInt(birthday, 10) > maxDate || minDate > parseInt(birthday, 10)) {
      return this.intl.t('registration_form.invalid_date');
    }

    return false;
  }

  get warningName() {
    if (this.firstName === undefined) {
      return false;
    }
    if (this.firstName.trim().split(' ').length === 1) {
      return this.intl.t('registration_form.empty_lastname');
    }
    return false;
  }

  get warningGender() {
    if (!this.birthday || !this.firstName) {
      return false;
    }
    if (!this.gender) {
      return this.intl.t('registration_form.empty_gender');
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
  // @ts-expect-error overrides property
  get login() {
    return this.email;
  }
  set login(value) {
    this.email = value;
  }
  @(task(function* (this: RegistrationFormComponent) {
    const user: LatestUserDTO = {
      name: this.firstName.trim(),
      email: this.email,
      gender: this.gender,
      avatar: '',
      bornYear: parseInt(this.birthday, 10),
      password: this.password,
    };
    try {
      const auth = getOwner(this).lookup('authenticator:firebase') as FirebaseAuthenticator;
      yield auth.registerUser(user.email, user.password);
    } catch(e) {
      this.errorMessage = e.message;
      yield this.registrationTask.cancelAll();
      return
    }

    yield this.loginTask.perform();

    const result = yield this.network.patchUserInfo(user);
    if (result.ok) {
        return;
    } else {
      const error = yield result.json();
      const key = error.errors.pop();
      if (this.intl.exists(`msg.validation.${key}`)) {
        this.errorMessage = this.intl.t(`msg.validation.${key}`);
      } else {
        this.errorMessage =
          key in ERRORS_MAP
            ? this.intl.t(ERRORS_MAP[key as keyof typeof ERRORS_MAP])
            : key;
      }
      yield this.registrationTask.cancelAll();
    }
  }).drop())
  registrationTask!: Task<any, any>;

  @action
  onSubmit(e: SubmitEvent & any) {
    e.preventDefault();
    this.registrationTask.perform();
  }

  @action
  setGender(e: SubmitEvent & any) {
    this.gender = e.target.value;
  }

  @action
  setAgreedStatus(e: Document & any) {
    this.agreed = e.target.checked;
  }
}
