import './index.css';
import Component from '@glimmer/component';
import { service } from '@ember/service';
import { gt } from 'ember-truth-helpers';
import type GamificationService from 'brn/services/gamification';

export default class StreakCounterComponent extends Component {
  @service('gamification') declare gamification: GamificationService;

  get streakClasses() {
    const classes = ['streak-counter'];
    if (this.gamification.currentStreak >= 5) {
      classes.push('streak-counter--high');
    }
    return classes.join(' ');
  }

  <template>
    {{#if (gt this.gamification.currentStreak 0)}}
      <div class={{this.streakClasses}} data-test-streak-counter role="status"
        aria-label="{{this.gamification.currentStreak}} day streak">
        <span class="streak-counter__flame" aria-hidden="true"><svg class="streak-counter__flame-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 23C7.03 23 3 18.97 3 14c0-3.13 1.76-6.08 4.5-8.5.69-.61 1.74-.1 1.62.8-.2 1.56.3 3.2 1.38 4.2.18.17.47.07.5-.16.27-1.83 1.15-3.5 2.5-4.84 1.6-1.6 2.86-3.6 3.3-5.8.12-.6.87-.78 1.2-.28C19.63 2.2 21 5.58 21 9c0 1.66-.34 3.25-1 4.7-.12.27.15.55.42.42.68-.33 1.24-.78 1.68-1.32.32-.39.92-.28 1.04.2.15.58.23 1.2.23 1.83 0 4.6-3.36 8.17-7.37 8.17H12Z" fill="#f97316"/><path d="M12 23c-2.21 0-4-1.79-4-4 0-1.5.8-2.86 2-3.7.46-.32 1.1-.02 1.12.54.02.72.36 1.4.92 1.82.14.11.34.04.38-.13.16-.73.56-1.4 1.12-1.9.67-.6 1.2-1.32 1.52-2.13.14-.36.64-.38.82-.04.38.72.62 1.5.62 2.34 0 .46-.06.92-.18 1.35-.05.18.12.33.28.24.26-.14.48-.34.65-.57.18-.24.54-.17.6.13.04.19.06.39.06.59 0 2.97-2.42 5.46-5.91 5.46Z" fill="#fbbf24"/></svg></span>
        <span class="streak-counter__text">{{this.gamification.currentStreak}}</span>
      </div>
    {{/if}}
  </template>
}
