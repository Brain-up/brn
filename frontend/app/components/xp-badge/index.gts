import './index.css';
import Component from '@glimmer/component';
import { service } from '@ember/service';
import type GamificationService from 'brn/services/gamification';

export default class XpBadgeComponent extends Component {
  @service('gamification') declare gamification: GamificationService;

  // SVG circular progress ring (36x36 viewBox)
  // Radius 16, circumference = 2 * PI * 16
  // strokeDashoffset = circumference * (1 - progress)

  get circumference() {
    return 2 * Math.PI * 16;
  }

  get strokeDashoffset() {
    return this.circumference * (1 - this.gamification.xpProgress / 100);
  }

  <template>
    <div class="xp-badge" data-test-xp-badge>
      <svg viewBox="0 0 36 36" class="xp-badge__ring">
        <circle cx="18" cy="18" r="16" fill="none" stroke="#e5e7eb" stroke-width="3" />
        <circle cx="18" cy="18" r="16" fill="none" stroke="#fbbf24" stroke-width="3"
          stroke-dasharray={{this.circumference}} stroke-dashoffset={{this.strokeDashoffset}}
          stroke-linecap="round" transform="rotate(-90 18 18)" />
        <text x="18" y="20" text-anchor="middle" class="xp-badge__level-text">
          {{this.gamification.level}}
        </text>
      </svg>
      <span class="xp-badge__xp-text hidden sm:inline">
        {{this.gamification.totalXp}} XP
      </span>
    </div>
  </template>
}
