import { Injectable } from '@angular/core';
import { AuthToken } from '@root/models/auth-token';
import { ALocaleStorage } from '@shared/storages/local-storage';

@Injectable()
export class AuthTokenService {
  public getAuthToken(): AuthToken | null {
    const authTokenBase64 = ALocaleStorage.AUTH_TOKEN.get();

    return authTokenBase64 ? JSON.parse(atob(authTokenBase64)) : null;
  }

  public setAuthToken(authToken: AuthToken): void {
    const authTokenBase64 = btoa(JSON.stringify(authToken));
    ALocaleStorage.AUTH_TOKEN.set(authTokenBase64);
  }

  public removeAuthToken(): void {
    ALocaleStorage.AUTH_TOKEN.remove();
  }
}
