
import { GetUsers } from '@admin/models/endpoints.model';
import { Exercise } from '@admin/models/exercise';
import { Roles } from '@admin/models/roles.type';
import { UserDailyDetailStatistics } from '@admin/models/user-daily-detail-statistics';
import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { UserMapped, UserWithNoAnalytics } from '@admin/models/user.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import dayjs, { Dayjs } from 'dayjs';
import { Observable } from 'rxjs';
import { map, pluck } from 'rxjs/operators';

@Injectable()
export class AdminApiService {
  private readonly httpClient = inject(HttpClient);

  public sendFormData(action: string, body: FormData): Observable<void> {
    return this.httpClient.post<void>(action, body);
  }

  public getExercisesBySubGroupId(subGroupId: number): Observable<Exercise[]> {
    return this.httpClient
      .get<{ data: Exercise[] }>(
        `/api/exercises?subGroupId=${subGroupId}`,
      )
      .pipe(pluck('data'));
  }

  public getUserWeeklyStatistics(
    userId: number,
    from: Dayjs,
    to: Dayjs,
  ): Observable<UserWeeklyStatistics[]> {
    return this.httpClient
      .get<{ data: UserWeeklyStatistics[] }>(
        `/api/v2/statistics/study/week?userId=${userId}&from=${from.format(
          'YYYY-MM-DDTHH:mm:ss',
        )}&to=${to.format('YYYY-MM-DDTHH:mm:ss')}`,
      )
      .pipe(pluck('data'));
  }

  public getUserYearlyStatistics(
    userId: number,
    from: Dayjs,
    to: Dayjs,
  ): Observable<UserYearlyStatistics[]> {
    return this.httpClient
      .get<{ data: UserYearlyStatistics[] }>(
        `/api/v2/statistics/study/year?userId=${userId}&from=${from.format(
          'YYYY-MM-DDTHH:mm:ss',
        )}&to=${to.format('YYYY-MM-DDTHH:mm:ss')}`,
      )
      .pipe(pluck('data'));
  }

  public getUsers(
    role: Roles = 'USER',
    withAnalytics: boolean = true,
  ): Observable<UserMapped[]> {
    let params = new HttpParams();
    params = params.append('role', role);
    params = params.append('withAnalytics', String(withAnalytics));

    return this.httpClient
      .get<GetUsers>('/api/users', {
        params,
      })
      .pipe(
        pluck('data'),
        map((userList: UserMapped[]) =>
          userList?.map((user, _i) => {
            user.age = dayjs().year() - user.bornYear;
            user.currentWeekChart = {
              data: [
                [
                  'data',
                  ...user.lastWeek.map(
                    ({ exercisingTimeSeconds }) => exercisingTimeSeconds,
                  ),
                ],
              ],
              option: {
                colors: {
                  data: (item) =>
                    USER_EXERCISING_PROGRESS_STATUS_COLOR[
                    user.lastWeek.map(({ progress }) => progress)[item.index]
                    ],
                },
                axis: { x: { show: false }, y: { show: false } },
                size: { height: 60, width: 140 },
                legend: { show: false },
                tooltip: { show: false },
                bar: { width: 8, radius: 4 },
              },
            };
            user.progress = user.diagnosticProgress.SIGNALS;
            return user;
          }).slice(0, 10),
        ),
      );
  }

  public getUsersWithNoAnalytics(
    role: Roles = 'USER',
    withAnalytics: boolean = false,
  ): Observable<UserWithNoAnalytics[]> {
    let params = new HttpParams();
    params = params.append('role', role);
    params = params.append('withAnalytics', String(withAnalytics));

    return this.httpClient
      .get<GetUsers>('/api/users', {
        params,
      })
      .pipe(
        pluck('data'),
        map((userList: UserWithNoAnalytics[]) => userList),
      );
  }

  public getSwaggerUi(): Observable<any> {
    return this.httpClient.get('/api/v3/api-docs', {
      responseType: 'text',
    });
  }

  public getUserDailyDetailStatistics(
    userId: number,
    day: Dayjs
  ): Observable<UserDailyDetailStatistics[]> {
    return this.httpClient
      .get<{ data: UserDailyDetailStatistics[] }>(
        `/api/v2/statistics/study/day?userId=${userId}&day=${day.format(
          'YYYY-MM-DDTHH:mm:ss',
        )}`,
      )
      .pipe(pluck('data'));
  }
}
