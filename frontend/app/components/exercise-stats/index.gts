import './index.css';
import Component from '@glimmer/component';
import { service } from '@ember/service';
import { IStatsExerciseStats } from 'brn/services/stats';
import type GamificationService from 'brn/services/gamification';
import { on } from '@ember/modifier';
import { t } from 'ember-intl';
import { or } from 'ember-truth-helpers';
import UiStatsIcon from 'brn/components/ui/stats/icon';
import UiButton from 'brn/components/ui/button';

interface ExerciseStatsSignature {
  Args: {
  stats: IStatsExerciseStats;
  onComplete: () => void;
  };
  Element: HTMLElement;
}

export default class ExerciseStatsComponent extends Component<ExerciseStatsSignature> {
  @service('gamification') declare gamification: GamificationService;

  get stats(): IStatsExerciseStats {
    return this.args.stats || {};
  }

  get sessionXp() {
    return this.gamification.sessionXp;
  }

  get accuracy() {
    const { rightAnswersCount, wrongAnswersCount } = this.stats;
    const total = (rightAnswersCount || 0) + (wrongAnswersCount || 0);
    if (total === 0) return 100;
    return Math.round(((rightAnswersCount || 0) / total) * 100);
  }

  get streakStatus() {
    return this.gamification.currentStreak;
  }

  get currentLevel() {
    return this.gamification.level;
  }

  get growthMessageKey() {
    if (this.accuracy >= 90) return 'gamification.summary_excellent';
    if (this.accuracy >= 60) return 'gamification.summary_good';
    return 'gamification.summary_keep_going';
  }

  get timeStats() {
    const { countedSeconds } = this.stats;
    const ms = countedSeconds * 1000;
    const totalSec = Math.floor(ms / 1000);
    const sec = totalSec % 60;
    const min = Math.floor(totalSec / 60);
    return { min, sec };
  }

  <template>
    <div
      class="exercise-stats__container rounded-large column flex flex-col items-center justify-between w-full h-full"
    >
      <div
        data-test-exercise-stats
        ...attributes
        class="justify-evenly flex flex-col flex-1 w-full"
      >
        <UiStatsIcon class="flex items-center justify-around w-full mb-1" />
        <div class="flex flex-col items-center w-full mb-6">
          <div class="flex items-start justify-around w-full">
            <ul class="leading-10 text-center">
              <li class="exercise-stats__item">{{t "statistics.time_board"}}:
                {{or this.timeStats.min 0}}
                {{t "statistics.time_min"}},
                {{or this.timeStats.sec 0}}
                {{t "statistics.time_sec"}}</li>
              <li class="exercise-stats__item">{{t "statistics.tasks"}}: {{or this.stats.rightAnswersCount 0}}</li>
              <li class="exercise-stats__item">{{t "statistics.repeats"}}: {{or this.stats.repeatsCount 0}}</li>
              <li class="exercise-stats__item">{{t "statistics.wrong_answers"}}:
                {{or this.stats.wrongAnswersCount 0}}</li>
            </ul>
            <ul class="exercise-stats__gamification leading-10 text-center mt-4">
              <li class="exercise-stats__item">{{t "gamification.xp_earned"}}: {{this.sessionXp}}</li>
              <li class="exercise-stats__item">{{t "gamification.accuracy"}}: {{this.accuracy}}%</li>
              <li class="exercise-stats__item">{{t "gamification.streak"}}: {{this.streakStatus}} {{t "gamification.days"}}</li>
              <li class="exercise-stats__item">{{t "gamification.level"}}: {{this.currentLevel}}</li>
            </ul>
          </div>
          <p class="exercise-stats__message text-center mt-2 text-lg font-semibold text-indigo-600">
            {{t this.growthMessageKey}}
          </p>
        </div>
        <div class="exercise-stats__action flex items-center justify-center">
          <UiButton
            data-test-continue
            class="w-auto px-8"
            @title={{t "statistics.continue"}}
            {{on "click" @onComplete}}
          />
        </div>
      </div>
    </div>
  </template>
}
