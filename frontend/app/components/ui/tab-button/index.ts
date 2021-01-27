import Component from '@glimmer/component';

interface UiTabButtonComponentArguments {
    title?: string,
    mode?: 'enabled' | 'disabled' | 'active'
}

export default class UiTabButtonComponent extends Component<UiTabButtonComponentArguments> {
  get classes() {
    let items = ['focus:outline-none rounded-lg text-lg w-full uppercase h-12'];
    if (this.args.mode === 'active') {
      items.push('active');
    }
    return items.join(' ');
  }
  get isDisabled() {
    return this.args.mode === 'disabled';
  }
}
