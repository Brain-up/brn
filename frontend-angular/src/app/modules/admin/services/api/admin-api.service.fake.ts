import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { Dayjs } from 'dayjs';
import * as dayjs from 'dayjs';
import { getRandomIntInclusive } from '@shared/helpers/get-random-int-inclusive';
import {
  UserExercisingProgressStatusType,
  USER_EXERCISING_PROGRESS_STATUS_COLOR,
} from '@admin/models/user-exercising-progress-status';
import { AdminApiService } from './admin-api.service';
import { DAYS_IN_WEEK, MONTHS_IN_YEAR } from '@shared/constants/common-constants';
import { SortType } from '@admin/models/sort';
import { User } from '@admin/models/user';
import { getRandomBool } from '@shared/helpers/get-random-bool';
import { getRandomString } from '@shared/helpers/get-random-string';

export class AdminApiServiceFake
  implements Pick<AdminApiService, 'getUserWeeklyStatistics' | 'getUserYearlyStatistics' | 'getUsers'>
{
  private readonly options: IOptions = {};

  constructor(o?: IOptions) {
    this.options.responseDelayInMs = o?.responseDelayInMs ?? 2000;
    this.options.isUserWeeklyStatisticsEmptyData = o?.isUserWeeklyStatisticsEmptyData;
    this.options.isUserYearlyStatisticsEmptyData = o?.isUserYearlyStatisticsEmptyData;
    this.options.exercisingTimeSecondsLimit = o?.exercisingTimeSecondsLimit ?? 5000;
    this.options.usersNumber = o?.usersNumber ?? 10;
  }

  public getUserWeeklyStatistics(userId: number, from: Dayjs, to: Dayjs): Observable<UserWeeklyStatistics[]> {
    const response: UserWeeklyStatistics[] = [];
    const daysNumber = to < dayjs() ? dayjs(to).daysInMonth() : dayjs().date();

    for (let dayNumber = 0; dayNumber < daysNumber; dayNumber++) {
      response.push({
        date: dayjs(from).add(dayNumber, 'day').toString(),
        exercisingTimeSeconds: getRandomIntInclusive(0, this.options.exercisingTimeSecondsLimit),
        progress: this.getRandomUserExercisingProgressStatusColor(),
      });
    }

    return of(this.options.isUserWeeklyStatisticsEmptyData ? [] : response).pipe(delay(this.options.responseDelayInMs));
  }

  public getUserYearlyStatistics(userId: number, from: Dayjs, to: Dayjs): Observable<UserYearlyStatistics[]> {
    const response: UserYearlyStatistics[] = [];
    const monthsNumber = to < dayjs() ? MONTHS_IN_YEAR : dayjs().month() + 1;

    for (let monthNumber = 0; monthNumber < monthsNumber; monthNumber++) {
      const today = dayjs(from).add(monthNumber, 'month');

      response.push({
        date: today.toString(),
        exercisingTimeSeconds: getRandomIntInclusive(0, this.options.exercisingTimeSecondsLimit),
        progress: this.getRandomUserExercisingProgressStatusColor(),
        exercisingDays: today.daysInMonth(),
      });
    }

    return of(this.options.isUserYearlyStatisticsEmptyData ? [] : response).pipe(delay(this.options.responseDelayInMs));
  }

  public getUsers(options: {
    pageNumber?: number;
    pageSize?: number;
    sort?: SortType;
    withAnalytics?: boolean;
  }): Observable<User[]> {
    const users: User[] = [];

    for (let i = 0; i < this.options.usersNumber; i++) {
      const name = getRandomString(7);
      const firstDone = dayjs(Date.now() - getRandomIntInclusive(0, 365 * 24 * 60 * 60 * 1000)).toISOString();
      const lastDone = dayjs(firstDone).add(1, 'month').toISOString();

      const lastWeek: number[] = [];
      for (let dayNumber = 0; dayNumber < DAYS_IN_WEEK; dayNumber++) {
        lastWeek.push(getRandomIntInclusive(0, 100));
      }

      users.push({
        active: getRandomBool(),
        bornYear: getRandomIntInclusive(1980, dayjs().subtract(10, 'year').year()),
        diagnosticProgress: { SIGNALS: getRandomBool() },
        email: `${name}@gmail.com`,
        firstDone,
        gender: getRandomBool() ? 'MALE' : 'FEMALE',
        id: i + 1,
        lastDone,
        lastWeek: lastWeek.map((value) => ({ value, progress: this.getRandomUserExercisingProgressStatusColor() })),
        name,
        workDayByLastMonth: getRandomIntInclusive(0, dayjs().subtract(1, 'month').daysInMonth()),
        isFavorite: getRandomBool(),
      });
    }

    return of(users).pipe(delay(this.options.responseDelayInMs));
  }

  private getRandomUserExercisingProgressStatusColor(): UserExercisingProgressStatusType {
    switch (getRandomIntInclusive(0, Object.keys(USER_EXERCISING_PROGRESS_STATUS_COLOR).length - 1)) {
      case 0:
        return 'BAD';

      case 1:
        return 'GOOD';

      case 2:
        return 'GREAT';
    }
  }
}

interface IOptions {
  responseDelayInMs?: number;
  isUserWeeklyStatisticsEmptyData?: boolean;
  isUserYearlyStatisticsEmptyData?: boolean;
  exercisingTimeSecondsLimit?: number;
  usersNumber?: number;
}
