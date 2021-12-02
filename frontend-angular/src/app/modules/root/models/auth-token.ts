// tslint:disable:variable-name

export class AuthToken {
  public access_token: string;
  public expires_in: number;
}

export interface User {
  displayName: string | null;
  email: string | null;
  isAnonymous: boolean;
  phoneNumber: string | null;
  photoURL: string | null;
  providerId: string;
  uid: string;
  
}
