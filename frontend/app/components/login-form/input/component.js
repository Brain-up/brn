import Component from '@glimmer/component';

export default class LoginFormInputComponent extends Component {
  get hasError() {
    const { model, name } = this.args;
    if (model[name] === undefined) {
      return false;
    }
    return (model[name] || '').trim().length === 0;
  }
  get value() {
    const { model, name } = this.args;
    return model[name];
  }
  set value(value) {
    const { model, name } = this.args;
    model[name] = (value||'').trim();
  }
}
