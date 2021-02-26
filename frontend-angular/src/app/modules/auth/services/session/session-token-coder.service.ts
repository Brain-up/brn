import { Injectable } from '@angular/core';
import { TokenOriginal, TokenShort } from '../../models/token';
import { SessionTokenMapperService } from './session-token-mapper.service';

@Injectable()
export class SessionTokenCoderService {

  static encodeToken(token: TokenOriginal): TokenShort {
    let {access_token, expires_in, token_start_date} = token;
    access_token = SessionTokenCoderService.encodeString(access_token);
    return SessionTokenMapperService.mapToShortToken({access_token, expires_in, token_start_date});
  }

  static decodeToken(token: TokenShort): TokenOriginal {
    let {t, e, s} = token;
    t = SessionTokenCoderService.decodeString(t);
    return SessionTokenMapperService.mapToOriginalToken({t, e, s});
  }

  static encodeString(input: string): string {
    const encoded = btoa(input);
    const reversed = SessionTokenCoderService.reverseString(encoded);
    return SessionTokenCoderService.mixLetters(reversed);
  }

  static decodeString(encoded: string): string {
    const unmixed = SessionTokenCoderService.mixLetters(encoded);
    const reversed = SessionTokenCoderService.reverseString(unmixed);
    return atob(reversed);
  }

  private static reverseString(word: string): string {
    return word.split('').reverse().join('');
  }

  private static mixLetters(word: string): string {
    return word.slice(-1) + word.slice(1, -1) + word.slice(0, 1);
  }
}
