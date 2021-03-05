import Component from '@glimmer/component';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';
import SubGroup from 'brn/models/subgroup';

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
  get group() {
    return this.args.group.id;
  }
  @action async mouseEnter() {
    this.inFocus = true;
    if (this.stats) {
      this.isFlipped = true;
    } else {
      const stats = await this.loadStats();
      this.stats = stats;
      if (this.inFocus && !this.isFlipped) {
        this.isFlipped = true;
      }
    }
  }
  @action mouseLeave() {
    this.inFocus = false;
    this.isFlipped = false;
  }

  async loadStats() {
    const stats = await this.network.subgroupStats(this.group);
    return stats;
  }
}
