import { attr } from '@ember-data/model';
import Model from '@ember-data/model';
import { DateTime } from 'luxon';

export enum PROGRESS {
  BAD = 'BAD',
  GOOD = 'GOOD',
  GREAT = 'GREAT',
}

export type UserExercisingProgressStatusType =
  | PROGRESS.BAD
  | PROGRESS.GOOD
  | PROGRESS.GREAT;

export interface IMonthTimeTrackItemData {
  progress: UserExercisingProgressStatusType;
  time: string;
  days: number;
  month: string;
  year: number;
  date: DateTime;
}

export default class UserWeeklyStatisticsModel extends Model {
  @attr('date') date!: string;
  @attr('number') exercisingTimeSeconds!: number;
  @attr('string') progress!: UserExercisingProgressStatusType;
}
