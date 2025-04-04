import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Subgroup } from '../../models/subgroup';

@Injectable()
export class SubGroupApiService {
  private readonly httpClient = inject(HttpClient);


  getSubgroupsBySeriesId(seriesId: string): Observable<Subgroup[]> {
    return this.httpClient.get<{ data: Subgroup[] }>(`/api/subgroups?seriesId=${seriesId}`).pipe(pluck('data'));
  }
}
