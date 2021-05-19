import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Login } from '../../models/login';
import { AuthToken } from '@root/models/auth-token';

@Injectable()
export class AuthenticationApiService {
  constructor(private readonly httpClient: HttpClient) {}

  public login(loginDto: Login): Observable<AuthToken> {
    return this.httpClient.post<AuthToken>('/api/brnlogin', loginDto);
  }
}
