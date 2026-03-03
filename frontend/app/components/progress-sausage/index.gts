import './index.css';
import Component from '@glimmer/component';
import htmlSafe from 'brn/helpers/html-safe';

interface ProgressSausageSignature {
  Args: {
  progressItems: { completedInCurrentCycle: boolean }[];
  };
  Element: HTMLElement;
}

export default class ProgressSausageComponent extends Component<ProgressSausageSignature> {
  get progressItems() {
    return this.args.progressItems ?? [];
  }

  get progressWidth() {
    return `width:${Math.round(this.progress * 100)}%;`;
  }

  get progress() {
    const items = this.progressItems;
    if (!items.length) {
      return 0;
    }
    return (
      items.filter(({ completedInCurrentCycle }) => completedInCurrentCycle)
        .length / items.length
    );
  }

  <template>
    <div class="c-progress-sausage" ...attributes>
      <div class="progress-bar">
        {{! template-lint-disable no-inline-styles style-concatenation }}
        <div
          class="progress-bar__value"
          style={{htmlSafe this.progressWidth}}
          data-test-progress-sausage
        ></div>
      </div>
    </div>
  </template>
}
