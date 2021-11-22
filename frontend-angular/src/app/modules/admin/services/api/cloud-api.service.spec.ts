import { CloudApiService } from './cloud-api.service';
import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { UploadForm } from '@admin/models/upload-form';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';

const baseUrl = '/api/cloud';
const filePath = '/random/path/example.json';

describe('CloudApiService', () => {
  let service: CloudApiService;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CloudApiService],
    });
    service = TestBed.inject(CloudApiService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call get upload form with file path', () => {
    let uploadForm: UploadForm | undefined;
    const url = `${baseUrl}/upload?filePath=${filePath}`;

    service.getUploadForm(filePath).subscribe((data) => {
      uploadForm = data;
    });

    const request = controller.expectOne(url);
    expect(request.request.method).toEqual('GET');
    request.flush('', { status: 204, statusText: 'No Data' });
    controller.verify();
  });

  it('should check call errors on get upload form with file path', () => {
    const errorEvent = new ErrorEvent('API error');
    const status = 500;
    const statusText = 'Server error';
    const url = `${baseUrl}/upload?filePath=${filePath}`;

    let actualError: HttpErrorResponse | undefined;

    service.getUploadForm(filePath).subscribe(
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

  it('should call get folders', () => {
    let folders: string[] | undefined;
    const url = `${baseUrl}/folders`;

    service.getFolders().subscribe((data) => {
      folders = data;
    });

    const request = controller.expectOne(url);
    expect(request.request.method).toEqual('GET');
    request.flush('', { status: 204, statusText: 'No Data' });
    controller.verify();
  });

  it('should check call errors on get folders', () => {
    const errorEvent = new ErrorEvent('API error');
    const status = 500;
    const statusText = 'Server error';
    const url = `${baseUrl}/folders`;

    let actualError: HttpErrorResponse | undefined;

    service.getFolders().subscribe(
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
