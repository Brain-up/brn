import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import NetworkService from 'brn/services/network';
import type { Subgroup as SubGroup } from 'brn/schemas/subgroup';
import { LinkTo } from '@ember/routing';
import { on } from '@ember/modifier';
import { concat } from '@ember/helper';
import { t } from 'ember-intl';
import htmlSafe from 'brn/helpers/html-safe';

interface ExerciseGroupSignature {
  Args: {
  group: SubGroup;
  };
  Element: HTMLElement;
}

export default class ExerciseGroup extends Component<ExerciseGroupSignature> {
  @service('network') network!: NetworkService;
  @tracked stats: null | {
    completedExercises: number;
    subGroupId: number;
    totalExercises: number;
  } = null;
  @tracked isFlipped = false;
  @tracked inFocus = false;
  private _loadingStats = false;
  get group() {
    return this.args.group.id;
  }
  @action async mouseEnter() {
    this.inFocus = true;
    if (this.stats) {
      this.isFlipped = true;
    } else if (!this._loadingStats) {
      this._loadingStats = true;
      try {
        const stats = await this.loadStats();
        this.stats = stats;
        if (this.inFocus && !this.isFlipped) {
          this.isFlipped = true;
        }
      } finally {
        this._loadingStats = false;
      }
    }
  }
  @action mouseLeave() {
    this.inFocus = false;
    this.isFlipped = false;
  }

  async loadStats() {
    const groupId = this.group;
    if (!groupId) {
      return null;
    }
    const stats = await this.network.subgroupStats(groupId);
    return stats;
  }

  get completedStats() {
    if (!this.stats) {
      return '';
    }
    return `${this.stats.completedExercises}/${this.stats.totalExercises}`;
  }

  <template>
    <LinkTo
      class="sm:m-2 flex flex-col m-1 overflow-hidden border-2 border-gray-200 rounded-lg shadow-lg" {{on "mouseenter" this.mouseEnter}}
      {{on "mouseleave" this.mouseLeave}}
      @route="group.series.subgroup"
      @model={{@group.id}}
      title={{@group.description}}
    >
      {{! template-lint-disable no-inline-styles style-concatenation }}
      <div
        class="sm:h-40 flex-shrink-0 h-16 bg-white bg-center bg-no-repeat bg-contain" style={{htmlSafe (concat "background-image: url(" @group.picture ");")}}
      >
        {{#if this.isFlipped}}
          {{! @ts-ignore }}
          <div
            class="sm:text-6xl w-full h-full pt-5 text-2xl font-extrabold text-center text-blue-600 bg-blue-300 bg-opacity-75" title={{concat @group.description ' / ' (t "exercise.progress") ': ' this.completedStats}}
          >{{this.completedStats}}</div>
        {{/if}}
      </div>
      <div class="sm:px-2 sm:py-2 flex items-center justify-center flex-1 px-1 py-1 bg-gray-200">
        <div
          class="sm:tracking-normal sm:leading-normal sm:text-base text-xs leading-tight tracking-tighter text-center break-words"
        >
          {{@group.name}}
        </div>
      </div>
    </LinkTo>
  </template>
}
