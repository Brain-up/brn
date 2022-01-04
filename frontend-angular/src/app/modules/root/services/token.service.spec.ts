import { AuthToken } from '../models/auth-token';
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

  it('should get token from localstore', () => {
    expect(service.getToken<AuthToken>()).toEqual(null);
  });

  it('should set token to localstore', () => {
    service.setToken<AuthToken>(authToken);
    expect(service.getToken<AuthToken>()).toEqual(authToken);
  });

  it('should get nothing if no token is set', () => {
    service.setToken<AuthToken>(undefined);
    expect(service.getToken<AuthToken>()).toEqual(null);
  });
});
