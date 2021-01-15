import Component from '@glimmer/component';

interface UiTabButtonComponentArguments {
    title?: string,
}

export default class UiTabButtonComponent extends Component<UiTabButtonComponentArguments> {
  get classes() {
    return 'focus:outline-none rounded w-1/6 uppercase h-12';      
  }
}
