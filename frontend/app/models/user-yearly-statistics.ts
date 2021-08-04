import { attr } from '@ember-data/model';
import UserWeeklyStatisticsModel from './user-weekly-statistics';

export default class UserYearlyStatisticsModel extends UserWeeklyStatisticsModel {
  @attr('number') exercisingDays!: number;

  get days() {
    return this.exercisingDays;
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    userYearlyStatistics: UserYearlyStatisticsModel;
  }
}
