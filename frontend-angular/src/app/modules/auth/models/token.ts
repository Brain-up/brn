export interface TokenShort {
  t: string;
  e: number;
  s?: number;
}

export interface TokenOriginal {
  access_token: string;
  expires_in: number;
  token_start_date?: number;
}
