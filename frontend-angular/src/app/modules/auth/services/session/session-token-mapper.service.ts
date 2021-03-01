import { Injectable } from '@angular/core';
import { TokenOriginal, TokenShort } from '../../models/token';

@Injectable()
export class SessionTokenMapperService {
  static mapToShortToken(token: TokenOriginal): TokenShort {
    return {
      t: token.access_token,
      e: token.expires_in,
      s: token.token_start_date
    };
  }

  static mapToOriginalToken(token: TokenShort): TokenOriginal {
    return {
      access_token: token.t,
      expires_in: token.e,
      token_start_date: token.s
    };
  }
}
