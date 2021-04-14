import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { Series } from '../../model/series';
import { Group } from '../../model/group';
import { Subgroup } from '../../model/subgroup';
import { Exercise } from '../../model/exercise';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  constructor(private httpClient: HttpClient) {
  }

  getGroups(locale?: string): Observable<Group[]> {
    let params = new HttpParams();
    if (locale) {
      params = params.append('locale', locale);
    }
    return this.httpClient.get<{ data: Group[] }>('/api/groups', {params}).pipe(
      pluck('data'),
    );
  }

  getSeriesByGroupId(groupId: string): Observable<Series[]> {
    return this.httpClient.get<{ data: Series[] }>('/api/series', {params: {groupId}}).pipe(
      pluck('data')
    );
  }

  getSubgroupsBySeriesId(seriesId: string): Observable<Subgroup[]> {
    return this.httpClient.get<{ data: Subgroup[] }>('/api/subgroups', {params: {seriesId}}).pipe(
      pluck('data')
    );
  }

  getExercisesBySubGroupId(subGroupId: string): Observable<Exercise[]> {
    return this.httpClient.get<any>('/api/admin/exercises', {params: {subGroupId}}).pipe(
      pluck('data')
    );
  }
}
