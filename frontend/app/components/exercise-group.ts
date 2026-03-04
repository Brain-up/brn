import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';
import type { Subgroup as SubGroup } from 'brn/schemas/subgroup';

interface IExerciseGroupArgs {
  group: SubGroup;
}

export default class ExerciseGroup extends Component<IExerciseGroupArgs> {
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
}
