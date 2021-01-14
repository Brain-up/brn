import Component from '@glimmer/component';

interface UiTabButtonComponentArguments {
    route?: string,
    title?: string,
    disabled?: boolean   
}

export default class UiTabButtonComponent extends Component<UiTabButtonComponentArguments> {
  get classes() {
    const optionsForAll = 'focus:outline-none rounded w-1/6 uppercase h-12';

    if (this.args.disabled)  {
      return `${optionsForAll} button-disabled`
    }
      return `${optionsForAll} border-gradient button-options
      hover:font-bold`
  }
}
