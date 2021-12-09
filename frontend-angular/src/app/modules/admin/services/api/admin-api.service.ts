import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { merge, Observable, zip } from 'rxjs';
import { Exercise } from '@admin/models/exercise';
import { map, pluck, mergeMap, concatMap, tap } from 'rxjs/operators';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { Dayjs } from 'dayjs';
import {
  User,
  UserMapped,
  UserWithNoAnalytics,
} from '@admin/models/user.model';
import { GetUsers } from '@admin/models/endpoints.model';
import { Roles } from '@admin/models/roles.type';
import * as dayjs from 'dayjs';
import { ILastWeekChartDataItem } from '@admin/models/last-week-chart-data-item';
import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';

@Injectable()
export class AdminApiService {
  private chartsData: ILastWeekChartDataItem[][];

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
  ): Observable<UserWithNoAnalytics[] | UserMapped[]> {
    let params = new HttpParams();
    params = params.append('role', role);
    params = params.append('withAnalytics', String(withAnalytics));

    if (withAnalytics) {
      return this.httpClient
        .get<GetUsers>('/api/admin/users', {
          params,
        })
        .pipe(
          pluck('data'),
          // tap((userList: UserMapped[]) =>
          //   userList.map((user, i) => {
          //     this.chartsData.push(
          //       user.lastWeek.map(({ exercisingTimeSeconds, progress }) => ({
          //         y: exercisingTimeSeconds,
          //         progress,
          //       })),
          //     );
          //     console.log('chart', this.chartsData);
          //   }),
          // ),
          map((userList: UserMapped[]) =>
            userList.map((user, i) => {
              // this.chartsData.push(
              //   user.lastWeek.map(({ exercisingTimeSeconds, progress }) => ({
              //     y: exercisingTimeSeconds,
              //     progress,
              //   })),
              // );
              (user.currentWeekChart = {
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
                    // data: (dataItem) =>
                    //   USER_EXERCISING_PROGRESS_STATUS_COLOR[
                    //     this.chartsData[i][dataItem.index].progress
                    //   ],
                  },
                  axis: { x: { show: false }, y: { show: false } },
                  size: { height: 60, width: 140 },
                  legend: { show: false },
                  tooltip: { show: false },
                  bar: { width: 8, radius: 4 },
                },
              }),
                (user.age = dayjs().year() - user.bornYear);
              return user;
            }),
          ),
        );
    } else {
      return this.httpClient
        .get<GetUsers>('/api/admin/users', {
          params,
        })
        .pipe(pluck('data'));
    }
  }
}
