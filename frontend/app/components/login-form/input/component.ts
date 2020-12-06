import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import IntlService from 'ember-intl/services/intl';

interface FormModel {
  [field: string]: string;
}

interface ILoginFormInputComponentArgs {
  model: FormModel;
  name: keyof FormModel;
  warning: string;
  type?: string;
  label?: string;
  placeholder?: string
}
export default class LoginFormInputComponent extends Component<ILoginFormInputComponentArgs> {
  @service('intl') intl!: IntlService;

  get hasError() {
    const { value } = this;
    if (value === undefined) {
      return false;
    }
    return (value || '').trim().length === 0;
  }

  get warning() {
    const { value } = this;
    const sumValue = (value || '').trim().length;

    if (sumValue >= this.maxlength - 1) {
      return `${this.intl.t('registration_form.warning_input_restriction')} - ${
        this.maxlength
      }`;
    }
    return this.args.warning || false;
  }

  get maxlength() {
    return 50;
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
    model[name] = (value || '').trim().slice(0, this.maxlength - 1);
  }
}
