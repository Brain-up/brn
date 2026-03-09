import './index.css';
import Component from '@glimmer/component';
import htmlSafe from 'brn/helpers/html-safe';
import { service } from '@ember/service';
import type GamificationService from 'brn/services/gamification';

interface ProgressSausageSignature {
  Args: {
  progressItems: { completedInCurrentCycle: boolean }[];
  };
  Element: HTMLElement;
}

export default class ProgressSausageComponent extends Component<ProgressSausageSignature> {
  @service('gamification') declare gamification: GamificationService;

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

  get progressPercent() {
    return Math.round(this.progress * 100);
  }

  get barClasses() {
    const classes = ['progress-bar'];
    if (this.gamification.showXpPopup) {
      classes.push('progress-bar--xp-burst');
    }
    if (this.progressPercent === 100) {
      classes.push('progress-bar--complete');
    }
    return classes.join(' ');
  }

  get valueClasses() {
    const classes = ['progress-bar__value'];
    if (this.gamification.showXpPopup) {
      classes.push('progress-bar__value--pulse');
    }
    if (this.progressPercent === 100) {
      classes.push('progress-bar__value--complete');
    }
    return classes.join(' ');
  }

  <template>
    <div class="c-progress-sausage" ...attributes>
      <div class={{this.barClasses}} role="progressbar"
        aria-valuenow={{this.progressPercent}} aria-valuemin="0" aria-valuemax="100">
        {{! template-lint-disable no-inline-styles style-concatenation }}
        <div
          class={{this.valueClasses}}
          style={{htmlSafe this.progressWidth}}
          data-test-progress-sausage
        ></div>
      </div>
      {{#if this.gamification.showXpPopup}}
        <span class="progress-bar__xp-badge" role="status" aria-live="polite" data-test-xp-popup>
          +{{this.gamification.lastXpGain}} XP
        </span>
      {{/if}}
    </div>
  </template>
}
