import { HttpErrorResponse } from '@angular/common/http';
import { Subgroup } from '@admin/models/subgroup';
import { SubGroupApiService } from './sub-group-api.service';
import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';

const baseUrl = '/api/subgroups';
const seriesId = '1234';

describe('SubGroupApiService', () => {
  let service: SubGroupApiService;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SubGroupApiService],
    });
    service = TestBed.inject(SubGroupApiService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call get series by group id', () => {
    let subgroup: Subgroup[] | undefined;
    const url = `${baseUrl}?seriesId=${seriesId}`;

    service.getSubgroupsBySeriesId(seriesId).subscribe((data) => {
      subgroup = data;
    });

    const request = controller.expectOne(url);
    expect(request.request.method).toEqual('GET');
    request.flush('', { status: 204, statusText: 'No Data' });
    controller.verify();
  });

  it('should check call errors on get series by group id', () => {
    const errorEvent = new ErrorEvent('API error');
    const status = 500;
    const statusText = 'Server error';
    const url = `${baseUrl}?seriesId=${seriesId}`;

    let actualError: HttpErrorResponse | undefined;

    service.getSubgroupsBySeriesId(seriesId).subscribe(
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
});
