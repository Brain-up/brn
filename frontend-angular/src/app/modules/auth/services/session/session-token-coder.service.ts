import { Injectable } from '@angular/core';
import { TokenOriginal, TokenShort } from '../../models/token';
import { SessionTokenMapperService } from './session-token-mapper.service';

@Injectable()
export class SessionTokenCoderService {

  static encodeToken(token: TokenOriginal): TokenShort {
    let {access_token, expires_in, token_start_date} = token;
    access_token = SessionTokenCoderService.encodeString(access_token);
    expires_in = SessionTokenCoderService.encodeNumber(expires_in);
    token_start_date = SessionTokenCoderService.encodeNumber(token_start_date);

    return SessionTokenMapperService.mapToShortToken({access_token, expires_in, token_start_date});
  }

  static decodeToken(token: TokenShort): TokenOriginal {
    let {t, e, s} = token;
    t = SessionTokenCoderService.decodeString(t);
    e = SessionTokenCoderService.decodeNumber(e);
    s = SessionTokenCoderService.decodeNumber(s);

    return SessionTokenMapperService.mapToOriginalToken({t, e, s});
  }

  static encodeString(input: string): string {
    return '';
  }

  static decodeString(input: string): string {
    return '';
  }

  static encodeNumber(input: number): number {
    return 0;
  }

  static decodeNumber(input: number): number {
    return 0;
  }
}
