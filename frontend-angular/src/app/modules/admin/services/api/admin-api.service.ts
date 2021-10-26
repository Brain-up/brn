import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Exercise } from '@admin/models/exercise';
import { pluck, map } from 'rxjs/operators';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { Dayjs } from 'dayjs';
import { SortType } from '@admin/models/sort';
import { PAGE_SIZE_DEFAULT } from '@shared/constants/common-constants';
import { User } from '@admin/models/user';
import { UsersData } from '@admin/models/users-data';

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
    sortBy: {
      name: SortType;
    };
    filters?: {
      isFavorite?: boolean;
      search?: string;
    };
    withAnalytics?: boolean;
  }): Observable<UsersData> {
    const params = new HttpParams({
      fromObject: {
        pageNumber: options?.pageNumber ? String(options.pageNumber) : undefined,
        pageSize: String(options?.pageSize ? options.pageSize : PAGE_SIZE_DEFAULT),
        'sortBy.name': options?.sortBy?.name ? String(options.sortBy.name) : undefined,
        'filters.favorite': options?.filters?.isFavorite ? String(options.filters.isFavorite) : undefined,
        'filters.search': options?.filters?.search ? String(options.filters.search) : undefined,
        withAnalytics: options?.withAnalytics ? String(options.withAnalytics) : undefined,
      },
    });

    return this.httpClient.get<{ data: User[] }>('/api/admin/users', { params })
                          .pipe(map(response => ({ total: response.data.length, users: response.data })));
  }
}
