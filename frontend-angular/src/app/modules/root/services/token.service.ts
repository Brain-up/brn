import { ALocaleStorage } from '@shared/storages/local-storage';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TokenService {
  public getToken<T>(localKey: string = 'AUTH_TOKEN'): T | null {
    const authTokenBase64 = ALocaleStorage[localKey].get();
    if (!authTokenBase64) {
      return null;
    }
    try {
      return JSON.parse(authTokenBase64);
    } catch (e) {
      return null;
    }
  }

  public setToken<T>(token: T, localKey: string = 'AUTH_TOKEN'): void {
    if (token) {
      const authTokenBase64 = JSON.stringify(token);
      ALocaleStorage[localKey].set(authTokenBase64);
    }
  }

  public removeToken(localKey: string = 'AUTH_TOKEN'): void {
    ALocaleStorage[localKey].remove();
  }
}
