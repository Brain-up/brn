import Component from '@glimmer/component';

export default class UiButtonComponent extends Component {
  optionsForEveryButton =
    'focus:outline-none font-bold uppercase rounded-md m-1';
  optionsPrimarySize = 'w-1/4 h-12';
  optionsSmallSize = 'w-1/6 h-12 text-sm';
  primaryHoverOptions = 'hover:from-blue-500 hover:to-purple-500';
  disabledOptions =
    'disabled:text-gray-500 disabled:from-gray-400 disabled:to-gray-400';
  primaryButton = `${this.optionsForEveryButton} ${this.primaryHoverOptions} bg-gradient-to-r from-blue-light to-purple-500 text-white ${this.optionsPrimarySize} ${this.disabledOptions} active:from-blue-dark`;
  secondaryButton = `${this.optionsForEveryButton} ${this.optionsPrimarySize} ${this.primaryHoverOptions} bg-gradient-to-r from-gray-300 to-gray-300 border-2 border-blue-light text-white disabled:text-gray-500 disabled:from-gray-300 disabled:to-gray-300 active:from-blue-dark`;
  primarySmallButton = `${this.primaryButton} ${this.optionsSmallSize}`;

  classOptions = {
    primaryBig: this.primaryButton,
    secondary: this.secondaryButton,
    primarySmall: this.primarySmallButton,
  };
}
