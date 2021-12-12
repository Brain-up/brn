import * as dayjs from 'dayjs';
import { Dayjs } from 'dayjs';
import { Exercise } from '@admin/models/exercise';
import { GetUsers } from '@admin/models/endpoints.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Roles } from '@admin/models/roles.type';
import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { UserMapped, UserWithNoAnalytics } from '@admin/models/user.model';

@Injectable()
export class AdminApiService {
  constructor(private readonly httpClient: HttpClient) {}

  public sendFormData(action: string, body: FormData): Observable<void> {
    return this.httpClient.post<void>(action, body);
  }

  public getExercisesBySubGroupId(subGroupId: number): Observable<Exercise[]> {
    return this.httpClient
      .get<{ data: Exercise[] }>(
        `/api/admin/exercises?subGroupId=${subGroupId}`,
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
        `/api/v2/admin/study/week?userId=${userId}&from=${from.format(
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
        `/api/v2/admin/study/year?userId=${userId}&from=${from.format(
          'YYYY-MM-DDTHH:mm:ss',
        )}&to=${to.format('YYYY-MM-DDTHH:mm:ss')}`,
      )
      .pipe(pluck('data'));
  }

  public getUsers(
    role: Roles = 'ROLE_USER',
    withAnalytics: boolean = true,
  ): Observable<UserMapped[]> {
    let params = new HttpParams();
    params = params.append('role', role);
    params = params.append('withAnalytics', String(withAnalytics));

    return this.httpClient
      .get<GetUsers>('/api/admin/users', {
        params,
      })
      .pipe(
        pluck('data'),
        map((userList: UserMapped[]) =>
          userList.map((user, i) => {
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
          }),
        ),
      );
  }

  public getUsersWithNoAnalytics(
    role: Roles = 'ROLE_USER',
    withAnalytics: boolean = false,
  ): Observable<UserWithNoAnalytics[]> {
    let params = new HttpParams();
    params = params.append('role', role);
    params = params.append('withAnalytics', String(withAnalytics));

    return this.httpClient
      .get<GetUsers>('/api/admin/users', {
        params,
      })
      .pipe(
        pluck('data'),
        map((userList: UserWithNoAnalytics[]) => userList),
      );
  }
}
