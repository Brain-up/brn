import { TokenOriginal } from './token';

export interface LoginSuccessModel extends TokenOriginal {
  token_type: string;
}
