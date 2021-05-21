import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { Dayjs } from 'dayjs';
import * as dayjs from 'dayjs';
import { getRandomIntInclusive } from '@shared/helpers/get-random-int-inclusive';
import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { AdminApiService } from './admin-api.service';
import { MONTHS_IN_YEAR } from '@shared/constants/common-constants';

export class AdminApiServiceFake
  implements Pick<AdminApiService, 'getUserWeeklyStatistics' | 'getUserYearlyStatistics'>
{
  private readonly options: IOptions = {};

  constructor(o?: IOptions) {
    this.options.responseDelayInMs = o?.responseDelayInMs ?? 2000;
    this.options.isUserWeeklyStatisticsEmptyData = o?.isUserWeeklyStatisticsEmptyData;
    this.options.isUserYearlyStatisticsEmptyData = o?.isUserYearlyStatisticsEmptyData;
    this.options.exercisingTimeSecondsLimit = o?.exercisingTimeSecondsLimit ?? 5000;
  }

  public getUserWeeklyStatistics(userId: number, from: Dayjs, to: Dayjs): Observable<UserWeeklyStatistics[]> {
    const response: UserWeeklyStatistics[] = [];

    for (let i = 0; i < (to < dayjs() ? dayjs(to).daysInMonth() : dayjs().date()); i++) {
      response.push({
        date: dayjs(from).add(i, 'day').toString(),
        exercisingTimeSeconds: getRandomIntInclusive(0, this.options.exercisingTimeSecondsLimit),
        progress: getRandomIntInclusive(0, USER_EXERCISING_PROGRESS_STATUS_COLOR.length - 1),
      });
    }

    return this.options.isUserWeeklyStatisticsEmptyData
      ? of([])
      : of(response).pipe(delay(this.options.responseDelayInMs));
  }

  public getUserYearlyStatistics(userId: number, from: Dayjs, to: Dayjs): Observable<UserYearlyStatistics[]> {
    const response: UserYearlyStatistics[] = [];

    for (let i = 0; i < (to < dayjs() ? MONTHS_IN_YEAR : dayjs().month() + 1); i++) {
      const today = dayjs(from).add(i, 'month');

      response.push({
        date: today.toString(),
        exercisingTimeSeconds: getRandomIntInclusive(0, this.options.exercisingTimeSecondsLimit),
        progress: getRandomIntInclusive(0, USER_EXERCISING_PROGRESS_STATUS_COLOR.length - 1),
        days: today.daysInMonth(),
      });
    }

    return this.options.isUserYearlyStatisticsEmptyData
      ? of([])
      : of(response).pipe(delay(this.options.responseDelayInMs));
  }
}

interface IOptions {
  responseDelayInMs?: number;
  isUserWeeklyStatisticsEmptyData?: boolean;
  isUserYearlyStatisticsEmptyData?: boolean;
  exercisingTimeSecondsLimit?: number;
}
