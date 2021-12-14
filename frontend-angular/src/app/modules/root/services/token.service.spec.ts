import { AuthToken, UserCredential } from '../models/auth-token';
import { TestBed } from '@angular/core/testing';
import { TokenService } from './token.service';

const authToken: AuthToken = {
  access_token: 'A token example',
  expires_in: 123456789,
};

describe('TokenService', () => {
  let service: TokenService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TokenService],
    });
    service = TestBed.inject(TokenService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get auth token from localstore', () => {
    expect(service.getToken<UserCredential>()).toEqual(null);
  });

  // it('should set auth token from localstore', () => {
  //   service.setAuthToken(authToken);
  //   expect(service.getToken<UserCredential>()).toEqual(authToken);
  // });

  // it('should remove auth token from localstore', () => {
  //   service.setAuthToken(authToken);
  //   service.removeAuthToken();
  //   expect(service.getToken<UserCredential>()).toEqual(null);
  // });
});
