export class AuthToken {
  public access_token: string;
  public expires_in: number;
}

export interface UserCredential {
  additionalUserInfo: AdditionalUserInfo;
  credential?: any;
  operationType: string;
  user: User;
}

export interface AdditionalUserInfo {
  providerId: string;
  isNewUser: boolean;
}

export interface User {
  apiKey: string;
  appName: string;
  authDomain: string;
  createdAt: string;
  displayName: string;
  email: string;
  emailVerified: boolean;
  isAnonymous: boolean;
  lastLoginAt: string;
  multiFactor: MultiFactor;
  phoneNumber?: any;
  photoURL?: any;
  providerData: ProviderData[];
  redirectEventId?: any;
  stsTokenManager: StsTokenManager;
  tenantId?: any;
  uid: string;
}

export interface MultiFactor {
  enrolledFactors: any[];
}

export interface ProviderData {
  displayName: string;
  email: string;
  phoneNumber?: any;
  photoURL?: any;
  providerId: string;
  uid: string;
}

export interface StsTokenManager {
  apiKey: string;
  accessToken: string;
  expirationTime: number;
  refreshToken: string;
}
