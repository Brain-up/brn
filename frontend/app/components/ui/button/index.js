import Component from '@glimmer/component';

export default class UiButtonComponent extends Component {
  optionsForEveryButton =
    'focus:outline-none font-bold uppercase rounded-md m-1';
  optionsPrimarySize = 'w-1/4 h-12';
  optionsSmallSize = 'w-1/6 h-12 text-sm';
  primaryHoverOptions = 'hover:from-blue-500 hover:to-purple-500';

  primaryButton = `${this.optionsForEveryButton} ${this.primaryHoverOptions} bg-gradient-to-r  from-blue-light to-purple-500 text-white ${this.optionsPrimarySize}`;
  primaryPressed = `${this.optionsForEveryButton}  ${this.primaryHoverOptions} bg-gradient-to-r from-blue-dark to-purple-500 text-white ${this.optionsPrimarySize}`;
  primaryDisabled = `${this.optionsForEveryButton} text-gray-500 bg-gray-400 ${this.optionsPrimarySize}`;

  secondaryButton = `${this.optionsForEveryButton} ${this.optionsPrimarySize} ${this.primaryHoverOptions} bg-gradient-to-r from-gray-300 to-gray-300 border-2 border-blue-light text-white`;
  secondaryPressed = `${this.optionsForEveryButton} ${this.optionsPrimarySize} ${this.primaryHoverOptions} bg-gradient-to-r from-blue-dark to-purple-500 border-2 border-blue-light text-white`;
  secondaryDisabled = `${this.optionsForEveryButton} ${this.optionsPrimarySize} text-gray-500 bg-gray-400 border-2 border-blue-light`;

  primarySmallButton = `${this.primaryButton}, ${this.optionsSmallSize}`;
  primarySmallButtonPressed = `${this.primaryPressed}, ${this.optionsSmallSize}`;
  primarySmallButtonDisabled = `${this.primaryDisabled}, ${this.optionsSmallSize}`;
}
