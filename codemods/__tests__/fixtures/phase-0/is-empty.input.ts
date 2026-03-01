import { isEmpty } from '@ember/utils';
import Component from '@glimmer/component';

export default class MyComponent extends Component {
  get isValid() {
    return !isEmpty(this.args.value);
  }
}
