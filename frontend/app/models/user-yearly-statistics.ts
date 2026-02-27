import { attr } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import UserWeeklyStatisticsModel from './user-weekly-statistics';

export default class UserYearlyStatisticsModel extends UserWeeklyStatisticsModel {
  declare [Type]: 'user-yearly-statistics';
  @attr('number') exercisingDays!: number;

  get days() {
    return this.exercisingDays;
  }
}

