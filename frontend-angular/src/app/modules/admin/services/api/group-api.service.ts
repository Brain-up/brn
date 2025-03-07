import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Group } from '../../models/group';

@Injectable()
export class GroupApiService {
  private readonly httpClient = inject(HttpClient);


  public getGroups(locale?: string): Observable<Group[]> {
    const params = new HttpParams({ fromObject: { locale } });

    return this.httpClient.get<{ data: Group[] }>('/api/groups', { params }).pipe(pluck('data'));
  }

  public getGroupById(id: number): Observable<Group> {
    return this.httpClient.get<{ data: Group }>(`/api/groups/${id}`).pipe(pluck('data'));
  }
}
