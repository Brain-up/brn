import { Injectable } from '@angular/core';
import { TokenOriginal, TokenShort } from '../../models/token';
import { LOCAL_STORAGE_AUTH_KEY } from '../../models/local-storage-auth-key';
import { SessionTokenCoderService } from './session-token-coder.service';
import { LoginSuccessModel } from '../../models/login-success.model';

@Injectable()
export class SessionTokenService {
  static getToken(): TokenOriginal {
    const authStateString = localStorage.getItem(LOCAL_STORAGE_AUTH_KEY);
    return authStateString ?
      SessionTokenCoderService.decodeToken(JSON.parse(authStateString) as TokenShort)
      : null;
  }

  static saveToken(tokenData: LoginSuccessModel): void {
    const sessionToken = {...tokenData};
    delete sessionToken.token_type;
    sessionToken.token_start_date = Date.now();
    sessionToken.expires_in = 1000 * 60 * 60 * 8; // = 8 hours, hardcoded on UI as it is not implemented correctly on back-end yet
    const encodedTokenData: TokenShort = SessionTokenCoderService.encodeToken(sessionToken as TokenOriginal);
    localStorage.setItem(LOCAL_STORAGE_AUTH_KEY, JSON.stringify(encodedTokenData));
  }

  static removeToken(): void {
    localStorage.removeItem(LOCAL_STORAGE_AUTH_KEY);
  }

  static isValidToken(tokenData: TokenOriginal) {
    return Date.now() < (tokenData.token_start_date + tokenData.expires_in);
  }
}
