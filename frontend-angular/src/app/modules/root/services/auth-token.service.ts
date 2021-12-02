import { Injectable } from '@angular/core';
import { RootObject, User } from '@root/models/auth-token';
import { ALocaleStorage } from '@shared/storages/local-storage';

@Injectable({ providedIn: 'root' })
export class AuthTokenService {
  public getAuthToken(): RootObject | null {
    const authTokenBase64 = ALocaleStorage.AUTH_TOKEN.get();
    return authTokenBase64 ? JSON.parse(authTokenBase64) : null;
  }

  public setAuthToken(authToken: User): void {
    if (authToken) {
      const authTokenBase64 = JSON.stringify(authToken);
      ALocaleStorage.AUTH_TOKEN.set(authTokenBase64);
    }
  }

  public removeAuthToken(): void {
    ALocaleStorage.AUTH_TOKEN.remove();
  }
}
