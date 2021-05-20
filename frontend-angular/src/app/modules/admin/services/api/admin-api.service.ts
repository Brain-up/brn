import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Exercise } from '@admin/models/exercise';
import { pluck } from 'rxjs/operators';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { Dayjs } from 'dayjs';

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
        `/api/admin/study/week?userId=${userId}&from=${from.toISOString()}&to=${to.toISOString()}`
      )
      .pipe(pluck('data'));
  }

  public getUserYearlyStatistics(userId: number, from: Dayjs, to: Dayjs): Observable<UserYearlyStatistics[]> {
    return this.httpClient
      .get<{ data: UserYearlyStatistics[] }>(
        `/api/admin/study/year?userId=${userId}&from=${from.toISOString()}&to=${to.toISOString()}`
      )
      .pipe(pluck('data'));
  }
}
