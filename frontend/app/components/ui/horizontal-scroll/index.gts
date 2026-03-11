import '../../../styles/horizontal-scroll.css';
import Component from '@glimmer/component';
import { action } from '@ember/object';
import { debounce, cancel } from '@ember/runloop';
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { on } from '@ember/modifier';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { fn } from '@ember/helper';
import { modifier } from 'ember-modifier';

interface UiHorizontalScrollSignature {
  Args: {};
  Blocks: {
    default: [];
  };
  Element: HTMLDivElement;
}

export default class UiHorizontalScroll extends Component<UiHorizontalScrollSignature> {
  @tracked containerElement: HTMLUListElement | null = null;
  @tracked scrollIteration = 0;
  debounceTimer: ReturnType<typeof debounce> | undefined = undefined;

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  registerContainer = modifier((element: HTMLUListElement) => {
    this.containerElement = element;
    return () => {
      this.containerElement = null;
    };
  });

  get showLeftScrollButton() {
    return this.hasScrollAtAll && (this.containerElement?.scrollLeft ?? 0) > 0;
  }

  get showRightScrollButton() {
    if (!this.hasScrollAtAll) {
      return false;
    }
    const el = this.containerElement;
    if (!el) return false;
    const scrollSize = el.offsetWidth + el.scrollLeft;
    return scrollSize < el.scrollWidth;
  }

  get hasScrollAtAll() {
    this.scrollIteration;
    if (!this.containerElement) {
      return false;
    }
    return this.containerElement.scrollWidth > this.containerElement.offsetWidth;
  }

  @action scroll(direction: 'right' | 'left') {
    if (!this.containerElement) return;
    const position = this.containerElement.scrollLeft;
    const offset = 150;
    const newPosition =
      direction === 'right' ? position + offset : position - offset;
    this.containerElement.scrollTo({
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
    <div class="hs-container" ...attributes>
      <div class="full relative overflow-hidden">
        {{#if this.showLeftScrollButton}}
          <div class="scroll-fade scroll-fade--left"></div>
          <button
            type="button"
            class="scroll-btn bg-purple-primary hover:opacity-75 focus:outline-hidden absolute left-0 z-20 flex items-center justify-center w-8 h-8 text-white rounded-full shadow-md"
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
          {{this.registerContainer}}
          {{on "scroll" this.onScroll}}
        >
          {{yield}}
        </ul>
        {{#if this.showRightScrollButton}}
          <div class="scroll-fade scroll-fade--right"></div>
          <button
            type="button"
            class="scroll-btn bg-purple-primary hover:opacity-75 focus:outline-hidden absolute right-0 z-20 flex items-center justify-center w-8 h-8 text-white rounded-full shadow-md"
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
