import Component from '@glimmer/component';
import { trackedRef } from 'ember-ref-bucket';
import { action } from '@ember/object';
import { debounce, cancel } from '@ember/runloop';
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { on } from '@ember/modifier';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { fn } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import sortBy from 'brn/helpers/sort-by';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import htmlSafe from 'brn/helpers/html-safe';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import createRef from 'ember-ref-bucket/modifiers/create-ref';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import UiTabButton from 'brn/components/ui/tab-button';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import autofitText from 'brn/modifiers/autofit-text';

export default class SubgroupNavigation extends Component {
  @trackedRef('container') container!: HTMLUListElement;
  @tracked scrollIteration = 0;
  debounceTimer: any = 0;
  get showLeftScrollButton() {
    return this.hasScrollAtAll && this.container?.scrollLeft > 0;
  }
  get showRightScrollButton() {
    if (!this.hasScrollAtAll) {
      return false;
    }
    const scrollSize = this.container?.offsetWidth + this.container?.scrollLeft;
    const result = scrollSize <= this.container?.scrollWidth;
    return result;
  }
  get hasScrollAtAll() {
    this.scrollIteration; // track iteration
    if (!this.container) {
      return false;
    }
    return this.container?.scrollWidth > this.container?.offsetWidth;
  }

  @action scroll(direction: 'right' | 'left') {
    const position = this.container.scrollLeft;
    const offset = 150;
    const newPosition =
      direction === 'right' ? position + offset : position - offset;
    this.container.scrollTo({
      top: 0,
      left: newPosition,
      behavior: 'smooth',
    });
  }

  @action onScroll() {
    cancel(this.debounceTimer);
    this.debounceTimer = debounce(this, this.updateScroll, 100);
  }

  updateScroll() {
    this.scrollIteration++;
  }

  willDestroy() {
    super.willDestroy();
    cancel(this.debounceTimer);
  }

  <template>
    <div ...attributes>
      <div class="full relative overflow-hidden">
        {{#if this.showLeftScrollButton}}
          <div class="scroll-fade scroll-fade--left"></div>
          <button
            type="button"
            class="scroll-btn bg-purple-primary hover:opacity-75 focus:outline-none absolute left-0 z-20 flex items-center justify-center w-8 h-8 text-white rounded-full shadow-md"
    
            {{on "click" (fn this.scroll "left")}}
            aria-label="Scroll left"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
            </svg>
          </button>
        {{/if}}
        <ul
          class="hs no-scrollbar"
          style={{htmlSafe "scroll-behavior: smooth;"}}
          {{createRef "container"}}
          {{on "scroll" this.onScroll}}
        >
          {{#each (sortBy "order" @group) as |exercise|}}
            <li class="item">
              <UiTabButton
                data-test-active-link={{exercise.name}}
                class="pl-3 pr-3"
                @small={{true}}
                @route="group.series.subgroup"
                @models={{array exercise.id}}
                @title={{exercise.name}}
                @tooltip={{exercise.description}}
                {{autofit-text exercise.name}}
              />
            </li>
          {{/each}}
        </ul>
        {{#if this.showRightScrollButton}}
          <div class="scroll-fade scroll-fade--right"></div>
          <button
            type="button"
            class="scroll-btn bg-purple-primary hover:opacity-75 focus:outline-none absolute right-0 z-20 flex items-center justify-center w-8 h-8 text-white rounded-full shadow-md"
    
            {{on "click" (fn this.scroll "right")}}
            aria-label="Scroll right"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
            </svg>
          </button>
        {{/if}}
      </div>
    </div>
  </template>
}
