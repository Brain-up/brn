import './index.css';
import Component from '@glimmer/component';
import { service } from '@ember/service';
import { gt } from 'ember-truth-helpers';
import type GamificationService from 'brn/services/gamification';

export default class StreakCounterComponent extends Component {
  @service('gamification') declare gamification: GamificationService;

  <template>
    {{#if (gt this.gamification.currentStreak 0)}}
      <div class="streak-counter" data-test-streak-counter>
        <span aria-hidden="true">&#128293;</span>
        <span class="streak-counter__text">{{this.gamification.currentStreak}}</span>
      </div>
    {{/if}}
  </template>
}
