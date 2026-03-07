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
        <span class="streak-counter__flame" aria-hidden="true">&#128293;</span>
        <span class="streak-counter__text">{{this.gamification.currentStreak}}</span>
      </div>
    {{/if}}
  </template>
}
