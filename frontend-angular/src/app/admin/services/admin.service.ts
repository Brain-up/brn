import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {pluck} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {Group, Series} from '../model/model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  constructor(private httpClient: HttpClient) {
  }

  getGroups(): Observable<Group[]> {
    return this.httpClient.get<{ data: Group[] }>('/api/groups').pipe(
      pluck('data'),
    );
  }

  getSeriesByGroupId(groupId: string): Observable<Series[]> {
    return this.httpClient.get<{ data: Series[] }>('/api/series', {params: {groupId}}).pipe(
      pluck('data')
    );
  }

}
