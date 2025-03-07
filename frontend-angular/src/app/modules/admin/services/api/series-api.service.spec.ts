import { HttpErrorResponse, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { Series } from '@admin/models/series';
import { SeriesApiService } from './series-api.service';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

const baseUrl = '/api/series';
const groupId = 1234;
const id = 1234;
const seriesId = 1234;

describe('SeriesApiService', () => {
  let service: SeriesApiService;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [],
    providers: [SeriesApiService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
});
    service = TestBed.inject(SeriesApiService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call get series by group id', () => {
    let series: Series[] | undefined;
    const url = `${baseUrl}?groupId=${groupId}`;

    service.getSeriesByGroupId(groupId).subscribe((data) => {
      series = data;
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
    const url = `${baseUrl}?groupId=${groupId}`;

    let actualError: HttpErrorResponse | undefined;

    service.getSeriesByGroupId(groupId).subscribe(
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

  it('should call get series by id', () => {
    let series: Series | undefined;
    const url = `${baseUrl}/${id}`;

    service.getSeriesById(id).subscribe((data) => {
      series = data;
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
    const url = `${baseUrl}/${id}`;

    let actualError: HttpErrorResponse | undefined;

    service.getSeriesById(id).subscribe(
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

  it('should call get file format by series id', () => {
    let fileFormat: string | undefined;
    const url = `${baseUrl}/fileFormat/${seriesId}`;

    service.getFileFormatBySeriesId(seriesId).subscribe((data) => {
      fileFormat = data;
    });

    const request = controller.expectOne(url);
    expect(request.request.method).toEqual('GET');
    request.flush('', { status: 204, statusText: 'No Data' });
    controller.verify();
  });

  it('should check call errors on file format by series id', () => {
    const errorEvent = new ErrorEvent('API error');
    const status = 500;
    const statusText = 'Server error';
    const url = `${baseUrl}/fileFormat/${seriesId}`;

    let actualError: HttpErrorResponse | undefined;

    service.getFileFormatBySeriesId(seriesId).subscribe(
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
