import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AdminApiService {
  constructor(private readonly httpClient: HttpClient) {}

  public sendFormData(action: string, body: FormData): Observable<{} | HttpErrorResponse> {
    return this.httpClient.post<{} | HttpErrorResponse>(action, body);
  }
}
