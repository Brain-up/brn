import Component from '@glimmer/component';
import { LinkTo } from '@ember/routing';
import { or } from 'ember-truth-helpers';

enum ButtonKind {
  primary = 'primary',
  secondary = 'secondary',
  outline = 'outline',
}

enum ButtonSize {
  small = 'small',
}

interface UiButtonSignature {
  Args: {
    kind?: `${ButtonKind}`;
    size?: `${ButtonSize}`;
    title?: string;
    type?: 'submit' | 'button';
    route?: string;
    model?: unknown;
    models?: unknown[];
    isLoading?: boolean;
    disabled?: boolean;
  };
  Blocks: {
    default: [];
  };
  Element: HTMLElement;
}

export default class UiButtonComponent extends Component<UiButtonSignature> {
  get isLoading() {
    return this.args.isLoading ?? false;
  }

  optionsForEveryButton =
    'btn-press focus:outline-hidden rounded-md font-semibold m-1 py-2 cursor-pointer';
  optionsPrimarySize = 'w-1/4 h-12';
  optionsSmallSize = 'w-1/4 h-10 text-sm';
  primaryHoverOptions = 'hover:from-blue-500 hover:to-purple-500';
  disabledOptions =
    'disabled:text-gray-500 disabled:from-gray-400 disabled:to-gray-400';
  get primaryButton() {
    return `${this.optionsForEveryButton} ${this.optionsPrimarySize} ${
      this.primaryHoverOptions
    } bg-gradient-to-r from-blue-light to-purple-500 text-white ${
      this.isSmall ? '' : this.optionsPrimarySize
    } ${this.disabledOptions} active:from-blue-dark`;
  }
  get secondaryButton() {
    return `${this.optionsForEveryButton} ${this.optionsPrimarySize} ${this.primaryHoverOptions} w-1/4 h-12 bg-gradient-to-r from-gray-300 to-gray-300 border-2 border-blue-light text-white disabled:text-gray-500 disabled:from-gray-300 disabled:to-gray-300 active:from-blue-dark`;
  }

  get outlineButton() {
    return `${this.optionsForEveryButton} ${this.optionsPrimarySize} text-lg border-gradient text-blue-light border-2 border-indigo-400 bg-gradient-to-r hover:border-transparent hover:from-blue-light hover:to-purple-500 hover:text-white disabled:text-gray-500 disabled:from-gray-300 disabled:to-gray-300`;
  }

  get primarySmallButton() {
    return `${this.primaryButton} ${this.optionsSmallSize}`;
  }

  get isSmall() {
    return this.args.size === ButtonSize.small;
  }

  get classes() {
    if (!this.args.kind || this.args.kind === ButtonKind.primary) {
      if (this.isSmall) {
        return this.primarySmallButton;
      } else {
        return this.primaryButton;
      }
    } else if (this.args.kind === ButtonKind.outline) {
      return this.outlineButton;
    } else {
      return this.secondaryButton;
    }
  }

  <template>
    {{#if @route}}
      {{#if @model}}
        <LinkTo
          class={{this.classes}}
          @route={{@route}}
          @model={{@model}}
          ...attributes
        >
          {{#if (has-block)}}
            {{yield}}
          {{else}}
            {{@title}}
          {{/if}}
        </LinkTo>
      {{else if @models}}
        <LinkTo
          class={{this.classes}}
          @route={{@route}}
          @models={{@models}}
          ...attributes
        >
          {{#if (has-block)}}
            {{yield}}
          {{else}}
            {{@title}}
          {{/if}}
        </LinkTo>
      {{else}}
        <LinkTo class={{this.classes}} @route={{@route}} ...attributes>
          {{#if (has-block)}}
            {{yield}}
          {{else}}
            {{@title}}
          {{/if}}
        </LinkTo>
      {{/if}}
    {{else}}
      <button type={{or @type "submit"}} class={{this.classes}} disabled={{or @disabled this.isLoading}} ...attributes>
        {{#if this.isLoading}}<span class="btn-spinner mr-2" aria-hidden="true"></span>{{/if}}
        {{#if (has-block)}}
          {{yield}}
        {{else}}
          {{@title}}
        {{/if}}
      </button>
    {{/if}}
  </template>
}
