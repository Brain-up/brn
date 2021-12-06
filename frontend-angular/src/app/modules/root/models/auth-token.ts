// tslint:disable:variable-name

export class AuthToken {
  public access_token: string;
  public expires_in: number;
}

export interface ProviderData {
  uid: string;
  displayName: string;
  photoURL?: any;
  email: string;
  phoneNumber?: any;
  providerId: string;
}

export interface StsTokenManager {
  apiKey: string;
  refreshToken: string;
  accessToken: string;
  expirationTime: number;
}

export interface MultiFactor {
  enrolledFactors: any[];
}

export interface User {
  uid: string;
  displayName: string;
  photoURL?: any;
  email: string;
  emailVerified: boolean;
  phoneNumber?: any;
  isAnonymous: boolean;
  tenantId?: any;
  providerData: ProviderData[];
  apiKey: string;
  appName: string;
  authDomain: string;
  stsTokenManager: StsTokenManager;
  redirectEventId?: any;
  lastLoginAt: string;
  createdAt: string;
  multiFactor: MultiFactor;
}

export interface AdditionalUserInfo {
  providerId: string;
  isNewUser: boolean;
}

export interface RootObject {
  user: User;
  credential?: any;
  additionalUserInfo: AdditionalUserInfo;
  operationType: string;
}
