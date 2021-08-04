import { attr } from '@ember-data/model';
import Model from '@ember-data/model';
import { DateTime } from 'luxon';
import { secondsTo } from 'brn/utils/seconds-to';

export enum PROGRESS {
  /* eslint-disable no-unused-vars */
  BAD = 'BAD',
  GOOD = 'GOOD',
  GREAT = 'GREAT',
}

export type UserExercisingProgressStatusType =
  | PROGRESS.BAD
  | PROGRESS.GOOD
  | PROGRESS.GREAT;

export default class UserWeeklyStatisticsModel extends Model {
  @attr('full-date') date!: DateTime;
  @attr('number') exercisingTimeSeconds!: number;
  @attr('string') progress!: UserExercisingProgressStatusType;

  get time(): string {
    return secondsTo(this.exercisingTimeSeconds, 'h:m:s');
  }

  get month(): string {
    return this.date.toFormat('MMMM');
  }

  get year(): number {
    return this.date.year;
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    userWeeklyStatistics: UserWeeklyStatisticsModel;
  }
}
