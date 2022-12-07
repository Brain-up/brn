import { Observable, of } from 'rxjs';
import { delay, map } from 'rxjs/operators';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { UserDailyDetailStatistics } from '@admin/models/user-daily-detail-statistics';
import * as dayjs from 'dayjs';
import { Dayjs } from 'dayjs';
import { getRandomIntInclusive } from '@shared/helpers/get-random-int-inclusive';
import {
  USER_EXERCISING_PROGRESS_STATUS_COLOR,
  UserExercisingProgressStatusType,
} from '@admin/models/user-exercising-progress-status';
import { AdminApiService } from './admin-api.service';
import { DAYS_IN_WEEK, MONTHS_IN_YEAR, } from '@shared/constants/common-constants';
import { User, UserMapped } from '@admin/models/user.model';
import { getRandomBool } from '@shared/helpers/get-random-bool';
import { getRandomString } from '@shared/helpers/get-random-string';

export class AdminApiServiceFake
  implements Pick<AdminApiService,
    'getUserWeeklyStatistics' | 'getUserYearlyStatistics' | 'getUserDailyDetailStatistics' | 'getUsers' | 'getUserDailyDetailStatistics'> {
  private readonly options: IOptions = {};

  constructor(o?: IOptions) {
    this.options.responseDelayInMs = o?.responseDelayInMs ?? 2000;
    this.options.isUserWeeklyStatisticsEmptyData =
      o?.isUserWeeklyStatisticsEmptyData;
    this.options.isUserYearlyStatisticsEmptyData =
      o?.isUserYearlyStatisticsEmptyData;
    this.options.isUserDailyDetailStatisticsEmptyData =
      o?.isUserDailyDetailStatisticsEmptyData;
    this.options.exercisingTimeSecondsLimit =
      o?.exercisingTimeSecondsLimit ?? 5000;
    this.options.usersNumber = o?.usersNumber ?? 10;
  }

  public getUserWeeklyStatistics(
    userId: number,
    from: Dayjs,
    to: Dayjs,
  ): Observable<UserWeeklyStatistics[]> {
    const response: UserWeeklyStatistics[] = [];
    const daysNumber = to < dayjs() ? dayjs(to).daysInMonth() : dayjs().date();

    for (let dayNumber = 0; dayNumber < daysNumber; dayNumber++) {
      response.push({
        date: dayjs(from).add(dayNumber, 'day').toString(),
        exercisingTimeSeconds: getRandomIntInclusive(
          0,
          this.options.exercisingTimeSecondsLimit,
        ),
        progress: this.getRandomUserExercisingProgressStatusColor(),
      });
    }

    return of(
      this.options.isUserWeeklyStatisticsEmptyData ? [] : response,
    ).pipe(delay(this.options.responseDelayInMs));
  }

  public getUserYearlyStatistics(
    userId: number,
    from: Dayjs,
    to: Dayjs,
  ): Observable<UserYearlyStatistics[]> {
    const response: UserYearlyStatistics[] = [];
    const monthsNumber = to < dayjs() ? MONTHS_IN_YEAR : dayjs().month() + 1;

    for (let monthNumber = 0; monthNumber < monthsNumber; monthNumber++) {
      const today = dayjs(from).add(monthNumber, 'month');

      response.push({
        date: today.toString(),
        exercisingTimeSeconds: getRandomIntInclusive(
          0,
          this.options.exercisingTimeSecondsLimit,
        ),
        progress: this.getRandomUserExercisingProgressStatusColor(),
        exercisingDays: today.daysInMonth(),
      });
    }

    return of(
      this.options.isUserYearlyStatisticsEmptyData ? [] : response,
    ).pipe(delay(this.options.responseDelayInMs));
  }

  public getUsers(): Observable<UserMapped[]> {
    const users: User[] = [];

    for (let i = 0; i < this.options.usersNumber; i++) {
      const name = getRandomString(7);
      const firstDone = dayjs(
        Date.now() - getRandomIntInclusive(0, 365 * 24 * 60 * 60 * 1000),
      ).toISOString();
      const lastDone = dayjs(firstDone).add(1, 'month').toISOString();

      const lastWeek: number[] = [];
      for (let dayNumber = 0; dayNumber < DAYS_IN_WEEK; dayNumber++) {
        lastWeek.push(getRandomIntInclusive(0, 100));
      }

      users.push({
        active: getRandomBool(),
        bornYear: getRandomIntInclusive(
          1980,
          dayjs().subtract(10, 'year').year(),
        ),
        diagnosticProgress: {SIGNALS: getRandomBool()},
        email: `${name}@gmail.com`,
        firstDone,
        gender: getRandomBool() ? 'MALE' : 'FEMALE',
        id: i + 1,
        lastDone,
        lastWeek: lastWeek.map((value) => ({
          date: '1234',
          exercisingTimeSeconds: value,
          progress: this.getRandomUserExercisingProgressStatusColor(),
        })),
        name,
        studyDaysInCurrentMonth: getRandomIntInclusive(
          0,
          dayjs().subtract(1, 'month').daysInMonth(),
        ),
        isFavorite: getRandomBool(),
        userId: '1234',
        spentTime: 10,
        doneExercises: 2,
      });
    }

    return of(users).pipe(
      delay(this.options.responseDelayInMs),
      map((userList: UserMapped[]) =>
        userList.map((user, i) => {
          user.age = dayjs().year() - user.bornYear;
          user.currentWeekChart = {
            data: [
              [
                'data',
                ...user.lastWeek.map(
                  ({exercisingTimeSeconds}) => exercisingTimeSeconds,
                ),
              ],
            ],
            option: {
              colors: {
                data: (item) =>
                  USER_EXERCISING_PROGRESS_STATUS_COLOR[
                    user.lastWeek.map(({progress}) => progress)[item.index]
                    ],
              },
              axis: {x: {show: false}, y: {show: false}},
              size: {height: 60, width: 140},
              legend: {show: false},
              tooltip: {show: false},
              bar: {width: 8, radius: 4},
            },
          };
          user.progress = user.diagnosticProgress.SIGNALS;
          return user;
        }),
      ),
    );
  }

  public getUserDailyDetailStatistics(
    userId: number,
    day: Dayjs
  ): Observable<UserDailyDetailStatistics[]> {
    const response: UserDailyDetailStatistics[] = [];
    response.push({
      seriesName: 'Слова Королёвой',
      allDoneExercises: 10,
      uniqueDoneExercises: 5,
      repeatedExercises: 8,
      doneExercisesSuccessfullyFromFirstTime: 1,
      listenWordsCount: 10
    });
    response.push({
      seriesName: 'Слова тестовые',
      allDoneExercises: 100,
      uniqueDoneExercises: 50,
      repeatedExercises: 50,
      doneExercisesSuccessfullyFromFirstTime: 50,
      listenWordsCount: 25
    });

    return of(
      this.options.isUserDailyDetailStatisticsEmptyData ? [] : response,
    ).pipe(delay(this.options.responseDelayInMs));
  }

  private getRandomUserExercisingProgressStatusColor(): UserExercisingProgressStatusType {
    switch (
      getRandomIntInclusive(
        0,
        Object.keys(USER_EXERCISING_PROGRESS_STATUS_COLOR).length - 1,
      )
      ) {
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
  isUserDailyDetailStatisticsEmptyData?: boolean;
  exercisingTimeSecondsLimit?: number;
  usersNumber?: number;
}
