import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Series } from '../../models/series';

@Injectable()
export class SeriesApiService {
  constructor(private readonly httpClient: HttpClient) {}

  public getSeriesByGroupId(groupId: number): Observable<Series[]> {
    return this.httpClient.get<{ data: Series[] }>(`/api/series?groupId=${groupId}`).pipe(pluck('data'));
  }

  public getSeriesById(id: number): Observable<Series> {
    return this.httpClient.get<{ data: Series }>(`/api/series/${id}`).pipe(pluck('data'));
  }

  public getFileFormatBySeriesId(seriesId: number): Observable<string> {
    return this.httpClient.get<{ data: string }>(`/api/series/fileFormat/${seriesId}`).pipe(pluck('data'));
  }
}
