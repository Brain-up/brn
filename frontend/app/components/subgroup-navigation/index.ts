import Component from '@glimmer/component';
import { trackedRef } from 'ember-ref-bucket';
import { action } from '@ember/object';
import { debounce, cancel } from '@ember/runloop';
import { tracked } from '@glimmer/tracking';


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
    const result = (scrollSize <= this.container?.scrollWidth);
    return result;
  }
  get hasScrollAtAll() {
    this.scrollIteration; // track iteration
    if (!this.container) {
      return false;
    }
    return this.container?.scrollWidth > this.container?.offsetWidth;
  }

  @action alignScrollPositions(node: HTMLButtonElement, [direction]:string[]) {
    const rect = this.container.getBoundingClientRect();
    if (direction === 'right') {
      node.style.left = (rect.width + rect.left + 10).toString() + 'px';
    } else {
      node.style.left = (rect.left - 20).toString() + 'px';
    }
    node.style.top = (rect.top + 30).toString() + 'px';
  }

  @action scroll(direction: "right" | "left") {
    const position = this.container.scrollLeft;
    const offset = 150;
    const newPosition = direction === 'right' ? position + offset : position - offset;
    this.container.scrollTo({
      top: 0,
      left: newPosition,
      behavior: 'smooth'
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
}
