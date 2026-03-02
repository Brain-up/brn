import Component from '@glimmer/component';
import { service } from '@ember/service';
import IntlService from 'ember-intl/services/intl';

interface FormModel {
  [field: string]: string;
}

interface LoginFormInputSignature {
  Args: {
  model: FormModel;
  name: keyof FormModel;
  warning: string;
  type?: string;
  label?: string;
  placeholder?: string;
  trimRight?: boolean;
  };
  Element: HTMLElement;
}

export default class LoginFormInputComponent extends Component<LoginFormInputSignature> {
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
    const safeValue = value || '';
    let normalizedValue = '';
    if (this.args.trimRight === false) {
      normalizedValue = safeValue.trimStart();
    } else {
      normalizedValue = safeValue.trim();
    }
    model[name] = normalizedValue.slice(0, this.maxlength - 1);
  }

  <template>
    {{#if @label}}
      <label for={{@name}} class="block mb-2 text-sm font-bold text-gray-700">
        {{@label}}
      </label>
    {{/if}}
    <Input
      maxlength={{this.maxlength}}
      name={{@name}}
      @value={{this.value}}
      class="{{if this.hasError "border-red-500"}}
        appearance-none border-b-2 w-full text-sm leading-tight focus:outline-none focus:shadow-outline py-2"
      id={{@name}}
      type={{or @type "text"}}
      placeholder={{or @placeholder ""}}
      required="true"
      ...attributes
    />
    {{#if this.warning}}
      <p
        data-test-warning-message={{@name}}
        warningMessage={{@warning}}
        class="mt-2 text-xs text-red-500"
      >
        {{this.warning}}
      </p>
    {{/if}}
  </template>
}
