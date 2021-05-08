import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Group } from '../../models/group';

@Injectable()
export class GroupApiService {
  constructor(private readonly httpClient: HttpClient) {}

  public getGroups(locale?: string): Observable<Group[]> {
    const queryParams = locale ? `?locale=${locale}` : '';

    return this.httpClient.get<{ data: Group[] }>('/api/groups' + queryParams).pipe(pluck('data'));
  }

  public getGroupById(id: number): Observable<Group> {
    return this.httpClient.get<{ data: Group }>(`/api/groups/${id}`).pipe(pluck('data'));
  }
}
