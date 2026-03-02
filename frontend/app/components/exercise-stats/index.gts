import Component from '@glimmer/component';
import { IStatsExerciseStats } from 'brn/services/stats';
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
  get stats(): IStatsExerciseStats {
    return this.args.stats || {};
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
      class="rounded-large column flex flex-col items-center justify-between w-full h-full"
    >
      <div
        data-test-exercise-stats
        ...attributes
        class="justify-evenly flex flex-col flex-1 w-full"
      >
        <UiStatsIcon class="flex items-center justify-around w-full mb-1" />
        <div class="flex items-center justify-around w-full mb-6">
          <ul class="leading-10 text-center">
            <li>{{t "statistics.time_board"}}:
              {{or this.timeStats.min 0}}
              {{t "statistics.time_min"}},
              {{or this.timeStats.sec 0}}
              {{t "statistics.time_sec"}}</li>
            <li>{{t "statistics.tasks"}}: {{or this.stats.rightAnswersCount 0}}</li>
            <li>{{t "statistics.repeats"}}: {{or this.stats.repeatsCount 0}}</li>
            <li>{{t "statistics.wrong_answers"}}:
              {{or this.stats.wrongAnswersCount}}</li>
          </ul>
        </div>
        <div class="flex items-center justify-center">
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
