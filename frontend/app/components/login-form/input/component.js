import Component from '@glimmer/component';

export default class LoginFormInputComponent extends Component {
  get hasError() {
    const { value } = this;
    if (value === undefined) {
      return false;
    }
    return (value || '').trim().length === 0;
  }

  get isValid() {
    const { value } = this;
    const sumValue = (value || '').trim().length;
    if (sumValue === `${this.maxlength - 1}`) {
      return true
    }
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
    model[name] = (value||'').trim().slice(0, this.maxlength - 1);
  }
}
