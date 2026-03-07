import './index.css';
import Component from '@glimmer/component';
import { service } from '@ember/service';
import type GamificationService from 'brn/services/gamification';

export default class XpPopupComponent extends Component {
  @service('gamification') declare gamification: GamificationService;

  <template>
    {{#if this.gamification.showXpPopup}}
      <div class="xp-popup" role="status" aria-live="polite" data-test-xp-popup>
        +{{this.gamification.lastXpGain}} XP
      </div>
    {{/if}}
  </template>
}
