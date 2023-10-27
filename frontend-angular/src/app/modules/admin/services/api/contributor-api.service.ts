import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Contributor, UploadContributorImage } from '@admin/models/contrubutor.model';
import { Observable } from 'rxjs';
import { GetContributors } from '@admin/models/endpoints.model';
import { map, pluck } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ContributorApiService {

  constructor(private httpClient: HttpClient) {
  }

  public getContributors(): Observable<Contributor[]> {
    return this.httpClient
      .get<GetContributors>('/api/contributors')
      .pipe(
        pluck('data'),
        map((contributorsList: Contributor[]) => contributorsList),
      );
  }

  public createContributor(
    payload: Contributor
  ): Observable<Contributor> {
    return this.httpClient.post<Contributor>(
      '/api/contributors',
      payload
    );
  }

  public updateContributor(
    contributorId,
    payload: Contributor
  ): Observable<Contributor> {
    return this.httpClient.put<Contributor>(
      `/api/contributors/${contributorId}`,
      payload
    );
  }

  public uploadContributorImage(
    payload: FormData
  ): Observable<UploadContributorImage> {
    return this.httpClient.post<UploadContributorImage>(
      '/api/cloud/upload/contributor/picture',
      payload
    );
  }
}
