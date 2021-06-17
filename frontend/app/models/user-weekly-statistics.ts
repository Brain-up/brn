import { attr } from '@ember-data/model';
import Model from '@ember-data/model';

export type UserExercisingProgressStatusType = 'BAD' | 'GOOD' | 'GREAT';

export default class UserWeeklyStatisticsModel extends Model {
  @attr('date') date!: Date;
  @attr('number') exercisingTimeSeconds!: number;
  @attr('string') progress!: UserExercisingProgressStatusType;
}
