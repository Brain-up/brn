import { attr } from '@ember-data/model';
import UserWeeklyStatisticsModel from './user-weekly-statistics';

export default class UserYearlyStatisticsModel extends UserWeeklyStatisticsModel {
  @attr('number') exercisingDays!: number;
}
