import { TestBed } from '@angular/core/testing';

import { ContributorApiService } from './contributor-api.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ContributorApiService', () => {
  let service: ContributorApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [],
    providers: [ContributorApiService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
});
    service = TestBed.inject(ContributorApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
