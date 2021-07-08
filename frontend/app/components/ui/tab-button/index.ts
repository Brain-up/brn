import Component from '@glimmer/component';

interface UiTabButtonComponentArguments {
  title?: string;
  mode?: 'enabled' | 'disabled' | 'active';
  route?: string;
  small?: boolean;
  models?: ArrayLike<any>;
}

export default class UiTabButtonComponent extends Component<UiTabButtonComponentArguments> {
  get classes() {
    let items = [
      'focus:outline-none rounded-lg w-full uppercase h-12 overflow-hidden',
    ];
    if (this.args.small) {
      items.push('text-xs');
    } else {
      items.push('text-lg');
    }
    if (this.args.mode === 'active') {
      items.push('active');
    }
    return items.join(' ');
  }
  get isDisabled() {
    return this.args.mode === 'disabled';
  }
}
