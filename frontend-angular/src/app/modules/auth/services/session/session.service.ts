import { Injectable } from '@angular/core';
import { LoginRequestModel } from '../../models/login-request.model';
import { LoginSuccessModel } from '../../models/login-success.model';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginFailureModel } from '../../models/login-failure.model';

@Injectable()
export class SessionService {
    createSession(loginModel: LoginRequestModel): Observable<LoginSuccessModel | HttpErrorResponse> {
        return this.httpClient.post<LoginSuccessModel | HttpErrorResponse>('/api/brnlogin', loginModel);
    }
    destroySession(): Observable<{} | HttpErrorResponse> {
      const headers = new HttpHeaders().set('Content-Type', 'text/plain; charset=utf-8');
      return this.httpClient.post<string>('/api/logout', null, {headers, responseType: 'text' as 'json'});
    }
    constructor(private httpClient: HttpClient) {}
}
