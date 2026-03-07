import './index.css';
import Component from '@glimmer/component';
import { service } from '@ember/service';
import type GamificationService from 'brn/services/gamification';
import type IntlService from 'ember-intl/services/intl';
import { t } from 'ember-intl';

export default class XpBadgeComponent extends Component {
  @service('gamification') declare gamification: GamificationService;
  @service('intl') declare intl: IntlService;

  // SVG circular progress ring (36x36 viewBox)
  // Radius 16, circumference = 2 * PI * 16
  // strokeDashoffset = circumference * (1 - progress)

  get circumference() {
    return 2 * Math.PI * 16;
  }

  get strokeDashoffset() {
    return this.circumference * (1 - this.gamification.xpProgress / 100);
  }

  get isEmpty() {
    return this.gamification.totalXp === 0;
  }

  get isNearLevelUp() {
    return this.gamification.xpProgress >= 80;
  }

  get badgeClasses() {
    const classes = ['xp-badge'];
    if (this.isEmpty) {
      classes.push('xp-badge--empty');
    }
    if (this.gamification.showXpPopup) {
      classes.push('xp-badge--pulse');
    }
    if (this.isNearLevelUp) {
      classes.push('xp-badge--near-levelup');
    }
    return classes.join(' ');
  }

  get tooltipText(): string {
    const level = this.gamification.level;
    const totalXp = this.gamification.totalXp;
    const nextXp = this.gamification.xpForNextLevel;
    const levelLabel = this.intl.t('gamification.level');
    return `${levelLabel} ${level} — ${totalXp}/${nextXp} XP`;
  }

  <template>
    <div class={{this.badgeClasses}} data-test-xp-badge
      title={{this.tooltipText}}>
      <svg viewBox="0 0 36 36" class="xp-badge__ring" role="img"
        aria-label="{{t 'gamification.level'}} {{this.gamification.level}}, {{this.gamification.totalXp}} XP">
        {{!-- Decorative background glow for empty state --}}
        {{#if this.isEmpty}}
          <circle cx="18" cy="18" r="16" fill="none" stroke="#fbbf24" stroke-width="3"
            opacity="0.2" class="xp-badge__hint-ring" />
        {{/if}}
        <circle cx="18" cy="18" r="16" fill="none" stroke="#e5e7eb" stroke-width="3" />
        <circle cx="18" cy="18" r="16" fill="none" stroke="#fbbf24" stroke-width="3"
          stroke-dasharray={{this.circumference}} stroke-dashoffset={{this.strokeDashoffset}}
          stroke-linecap="round" transform="rotate(-90 18 18)"
          class="xp-badge__progress" />
        {{!-- Star SVG for empty state, level number otherwise --}}
        {{#if this.isEmpty}}
          <polygon points="18,8 20.5,14.5 27,15 22,19.5 23.5,26 18,22.5 12.5,26 14,19.5 9,15 15.5,14.5"
            fill="#fbbf24" class="xp-badge__star-icon" />
        {{else}}
          <text x="18" y="18" text-anchor="middle" dominant-baseline="central" class="xp-badge__level-text">
            {{this.gamification.level}}
          </text>
        {{/if}}
      </svg>
      <span class="xp-badge__xp-text hidden sm:inline">
        {{#if this.isEmpty}}
          {{t "gamification.xp_badge_start"}}
        {{else}}
          {{this.gamification.totalXp}} XP
        {{/if}}
      </span>
    </div>
  </template>
}
