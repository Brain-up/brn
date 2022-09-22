import { User } from '@admin/models/user.model';
import { Dayjs } from 'dayjs';
import { AdminApiService } from './admin-api.service';
import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { Exercise } from '@admin/models/exercise';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import * as dayjs from 'dayjs';

const baseUrl = '/api';
const baseUrlV2 = '/api/v2';
const action = 'action';
const body: FormData = new FormData();
const subGroupId = 1234;
const userId = 1234;
const from: Dayjs = dayjs('2019-01-01T00:00:00');
const to: Dayjs = dayjs('2022-01-10T00:00:00');

describe('AdminApiService', () => {
  let service: AdminApiService;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminApiService],
    });
    service = TestBed.inject(AdminApiService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call post form data', () => {
    const url = action;

    service.sendFormData(action, body).subscribe(() => {});

    const request = controller.expectOne(url);
    expect(request.request.method).toEqual('POST');
    request.flush('', { status: 204, statusText: 'No Data' });
    controller.verify();
  });

  it('should check call errors on post form data', () => {
    const errorEvent = new ErrorEvent('API error');
    const status = 500;
    const statusText = 'Server error';
    const url = action;

    let actualError: HttpErrorResponse | undefined;

    service.sendFormData(action, body).subscribe(
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

    controller.expectOne(url).error(errorEvent, { status, statusText });

    if (!actualError) {
      throw new Error('Error needs to be defined');
    }
    expect(actualError.error).toBe(errorEvent);
    expect(actualError.status).toBe(status);
    expect(actualError.statusText).toBe(statusText);
  });

  it('should call get exercises by subgroup id', () => {
    let exercise: Exercise[] | undefined;
    const url = `${baseUrl}/exercises/search?subGroupId=${subGroupId}`;

    service.getExercisesBySubGroupId(subGroupId).subscribe((data) => {
      exercise = data;
    });

    const request = controller.expectOne(url);
    expect(request.request.method).toEqual('GET');
    request.flush('', { status: 204, statusText: 'No Data' });
    controller.verify();
  });

  it('should check call errors on get get exercises by subgroup id', () => {
    const errorEvent = new ErrorEvent('API error');
    const status = 500;
    const statusText = 'Server error';
    const url = `${baseUrl}/exercises/search?subGroupId=${subGroupId}`;

    let actualError: HttpErrorResponse | undefined;

    service.getExercisesBySubGroupId(subGroupId).subscribe(
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

    controller.expectOne(url).error(errorEvent, { status, statusText });

    if (!actualError) {
      throw new Error('Error needs to be defined');
    }
    expect(actualError.error).toBe(errorEvent);
    expect(actualError.status).toBe(status);
    expect(actualError.statusText).toBe(statusText);
  });

  it('should call get user weekly statistics', () => {
    let userWeeklyStatistics: UserWeeklyStatistics[] | undefined;
    const url = `${baseUrlV2}/statistics/study/week?userId=${userId}&from=${from.format(
      'YYYY-MM-DDTHH:mm:ss',
    )}&to=${to.format('YYYY-MM-DDTHH:mm:ss')}`;

    service.getUserWeeklyStatistics(userId, from, to).subscribe((data) => {
      userWeeklyStatistics = data;
    });

    const request = controller.expectOne(url);
    expect(request.request.method).toEqual('GET');
    request.flush('', { status: 204, statusText: 'No Data' });
    controller.verify();
  });

  it('should check call errors on get user weekly statistics', () => {
    const errorEvent = new ErrorEvent('API error');
    const status = 500;
    const statusText = 'Server error';
    const url = `${baseUrlV2}/statistics/study/week?userId=${userId}&from=${from.format(
      'YYYY-MM-DDTHH:mm:ss',
    )}&to=${to.format('YYYY-MM-DDTHH:mm:ss')}`;

    let actualError: HttpErrorResponse | undefined;

    service.getUserWeeklyStatistics(userId, from, to).subscribe(
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

    controller.expectOne(url).error(errorEvent, { status, statusText });

    if (!actualError) {
      throw new Error('Error needs to be defined');
    }
    expect(actualError.error).toBe(errorEvent);
    expect(actualError.status).toBe(status);
    expect(actualError.statusText).toBe(statusText);
  });

  it('should call get users', () => {
    let usersData: User[] | undefined;
    const url = `${baseUrl}/users/search`;

    service.getUsers().subscribe((data) => {
      usersData = data;
    });

    const request = controller.expectOne(
      `${url}?role=ROLE_USER&withAnalytics=true`,
    );
    expect(request.request.method).toEqual('GET');
    request.flush('', { status: 204, statusText: 'No Data' });
    controller.verify();
  });

  it('should check call errors on get users', () => {
    const errorEvent = new ErrorEvent('API error');
    const status = 500;
    const statusText = 'Server error';
    const url = `${baseUrl}/users/search`;

    let actualError: HttpErrorResponse | undefined;

    service.getUsers().subscribe(
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

    controller
      .expectOne(
        `${url}?role=ROLE_USER&withAnalytics=true`,
      )
      .error(errorEvent, { status, statusText });

    if (!actualError) {
      throw new Error('Error needs to be defined');
    }
    expect(actualError.error).toBe(errorEvent);
    expect(actualError.status).toBe(status);
    expect(actualError.statusText).toBe(statusText);
  });
});
