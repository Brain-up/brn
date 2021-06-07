import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginData } from '../../models/login-data';
import { AuthToken } from '@root/models/auth-token';

@Injectable()
export class AuthenticationApiService {
  constructor(private readonly httpClient: HttpClient) {}

  public login(data: LoginData): Observable<AuthToken> {
    return this.httpClient.post<AuthToken>('/api/brnlogin', data);
  }
}
