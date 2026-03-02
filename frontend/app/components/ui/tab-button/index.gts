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
    const items = [
      'btn-press focus:outline-none rounded-lg w-full uppercase h-12 overflow-hidden flex items-center justify-center text-center',
    ];
    if (this.args.small) {
      items.push('text-xs');
    } else {
      items.push('sm:text-sm text-xs');
    }
    if (this.args.mode === 'active') {
      items.push('active');
    }
    return items.join(' ');
  }
  get isDisabled() {
    return this.args.mode === 'disabled';
  }

  <template>
    {{#if @route}}
    
      <LinkTo
        @tagName="button"
        type="button"
        @route={{@route}}
        @models={{@models}}
        class="{{this.classes}}"
        disabled={{this.isDisabled}}
        title={{@tooltip}}
        ...attributes
      >
        {{#if (has-block)}}
          {{yield}}
        {{else}}
          {{@title}}
        {{/if}}
      </LinkTo>
    
    {{else}}
    
      <button
        type="button"
        class="{{this.classes}}"
        disabled={{this.isDisabled}}
        title={{@tooltip}}
        ...attributes
      >
        {{#if (has-block)}}
          {{yield}}
        {{else}}
          {{@title}}
        {{/if}}
      </button>
    
    {{/if}}
  </template>
}
