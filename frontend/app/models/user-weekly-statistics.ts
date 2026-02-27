import { attr } from '@warp-drive-mirror/legacy/model';
import Model from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import { DateTime } from 'luxon';
import { secondsTo } from 'brn/utils/seconds-to';
import UserDataService from 'brn/services/user-data';
import { inject as service } from '@ember/service';

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
  declare [Type]: 'user-weekly-statistics';
  @attr('full-date') date!: DateTime;
  @attr('number') exercisingTimeSeconds!: number;
  @attr('string') progress!: UserExercisingProgressStatusType;
  @service('user-data') userData!: UserDataService;

  get time(): string {
    return secondsTo(this.exercisingTimeSeconds, 'h:m:s');
  }

  get month(): string {
    return this.date.toFormat('MMMM', {
      locale: this.userData.activeLocale,
    });
  }

  get year(): number {
    return this.date.year;
  }
}

