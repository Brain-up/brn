import { AuthToken } from './../models/auth-token';
import { TestBed } from '@angular/core/testing';
import { AuthTokenService } from './auth-token.service';

const authToken: AuthToken = {
  access_token: 'A token example',
  expires_in: 123456789,
};

describe('AuthTokenService', () => {
  let service: AuthTokenService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthTokenService],
    });
    service = TestBed.inject(AuthTokenService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get auth token from localstore', () => {
    expect(service.getAuthToken()).toEqual(null);
  });

  // it('should set auth token from localstore', () => {
  //   service.setAuthToken(authToken);
  //   expect(service.getAuthToken()).toEqual(authToken);
  // });

  // it('should remove auth token from localstore', () => {
  //   service.setAuthToken(authToken);
  //   service.removeAuthToken();
  //   expect(service.getAuthToken()).toEqual(null);
  // });
});
