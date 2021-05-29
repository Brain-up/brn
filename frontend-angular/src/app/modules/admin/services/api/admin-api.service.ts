import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Exercise } from '@admin/models/exercise';
import { pluck } from 'rxjs/operators';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { Dayjs } from 'dayjs';
import { SortType } from '@admin/models/sort';
import { User } from '@admin/models/user';

@Injectable()
export class AdminApiService {
  constructor(private readonly httpClient: HttpClient) {}

  public sendFormData(action: string, body: FormData): Observable<void> {
    return this.httpClient.post<void>(action, body);
  }

  public getExercisesBySubGroupId(subGroupId: number): Observable<Exercise[]> {
    return this.httpClient
      .get<{ data: Exercise[] }>(`/api/admin/exercises?subGroupId=${subGroupId}`)
      .pipe(pluck('data'));
  }

  public getUserWeeklyStatistics(userId: number, from: Dayjs, to: Dayjs): Observable<UserWeeklyStatistics[]> {
    return this.httpClient
      .get<{ data: UserWeeklyStatistics[] }>(
        `/api/admin/study/week?userId=${userId}&from=${from.format('YYYY-MM-DD')}&to=${to.format('YYYY-MM-DD')}`
      )
      .pipe(pluck('data'));
  }

  public getUserYearlyStatistics(userId: number, from: Dayjs, to: Dayjs): Observable<UserYearlyStatistics[]> {
    return this.httpClient
      .get<{ data: UserYearlyStatistics[] }>(
        `/api/admin/study/year?userId=${userId}&from=${from.format('YYYY-MM-DD')}&to=${to.format('YYYY-MM-DD')}`
      )
      .pipe(pluck('data'));
  }

  public getUsers(options?: {
    pageNumber?: number;
    pageSize?: number;
    sort?: SortType;
    withAnalytics?: boolean;
  }): Observable<User[]> {
    const pageNumber = options.pageNumber ?? 1;
    const pageSize = options.pageSize ?? 10;
    const sort = options.sort ?? 'asc';
    const withAnalytics = options.withAnalytics ?? true;

    return this.httpClient
      .get<{ data: User[] }>(
        `/api/admin/users?withAnalytics=${withAnalytics}&pageNumber=${pageNumber}&pageSize=${pageSize}&sort=${sort}`
      )
      .pipe(pluck('data'));
  }
}
