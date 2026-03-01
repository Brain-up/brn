import Component from '@glimmer/component';

export default class MyComponent extends Component {
  get isValid() {
    return !// TODO: Ember's isEmpty also returns true for NaN and non-array objects with length === 0
    (this.args.value == null || this.args.value === '' || Array.isArray(this.args.value) && this.args.value.length === 0);
  }
}
