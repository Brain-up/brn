import { AuthToken } from './../../../root/models/auth-token';
import { LoginData } from './../../models/login-data';
import { TestBed } from '@angular/core/testing';
import { AuthenticationApiService } from './authentication-api.service';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';

const baseUrl = '/api/brnlogin';
const data: LoginData = {
  grant_type: 'password',
  password: 'admin',
  username: 'admin@admin.com',
};

describe('AuthenticationApiService', () => {
  let service: AuthenticationApiService;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthenticationApiService],
    });
    service = TestBed.inject(AuthenticationApiService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call login with credentials', () => {
    let authToken: AuthToken | undefined;

    service.login(data).subscribe((token) => {
      authToken = token;
    });

    const request = controller.expectOne(baseUrl);
    expect(request.request.method).toEqual('POST');
    request.flush('', { status: 204, statusText: 'No Data' });
    controller.verify();
  });

  it('should check call errors', () => {
    const errorEvent = new ErrorEvent('API error');
    const status = 500;
    const statusText = 'Server error';

    let actualError: HttpErrorResponse | undefined;

    service.login(data).subscribe(
      () => {
        fail('Next handler must not be called');
      },
      (error) => {
        actualError = error;
      },
      () => {
        fail('Complete handler must not be called');
      },
    );

    controller.expectOne(baseUrl).error(errorEvent, { status, statusText });

    if (!actualError) {
      throw new Error('Error needs to be defined');
    }
    expect(actualError.error).toBe(errorEvent);
    expect(actualError.status).toBe(status);
    expect(actualError.statusText).toBe(statusText);
  });
});
