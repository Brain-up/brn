import { attr } from '@ember-data/model';
import Model from '@ember-data/model';

export type UserExercisingProgressStatusType = 'BAD' | 'GOOD' | 'GREAT';

export interface IMonthTimeTrackItemData {
  progress: UserExercisingProgressStatusType;
  time: string;
  days: number;
  month: string;
  year: number;
  date: moment.Moment;
}

export default class UserWeeklyStatisticsModel extends Model {
  @attr('date') date!: Date;
  @attr('number') exercisingTimeSeconds!: number;
  @attr('string') progress!: UserExercisingProgressStatusType;
}
