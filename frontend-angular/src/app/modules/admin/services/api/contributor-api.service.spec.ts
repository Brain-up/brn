import { TestBed } from '@angular/core/testing';

import { ContributorApiService } from './contributor-api.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ContributorApiService', () => {
  let service: ContributorApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ContributorApiService]
    });
    service = TestBed.inject(ContributorApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
