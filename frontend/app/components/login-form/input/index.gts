import Component from '@glimmer/component';
import { service } from '@ember/service';
import IntlService from 'ember-intl/services/intl';
import { or } from 'ember-truth-helpers';
import { Input } from '@ember/component';

interface LoginFormInputSignature {
  Args: {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  model: any;
  name: string;
  warning?: string | boolean;
  type?: string;
  label?: string | boolean;
  placeholder?: string;
  trimRight?: boolean;
  };
  Element: HTMLInputElement;
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

  get value(): string | undefined {
    const { model, name } = this.args;
    if (!model) {
      return undefined;
    }
    return model[name] as string | undefined;
  }

  set value(value: string | undefined) {
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
        appearance-none border-b-2 w-full text-sm leading-tight focus:outline-hidden focus:ring-2 focus:ring-blue-500/50 py-2"
      id={{@name}}
      type={{or @type "text"}}
      placeholder={{or @placeholder ""}}
      required="true"
      ...attributes
    />
    {{#if this.warning}}
      <p
        data-test-warning-message={{@name}}
        class="mt-2 text-xs text-red-500"
      >
        {{this.warning}}
      </p>
    {{/if}}
  </template>
}
