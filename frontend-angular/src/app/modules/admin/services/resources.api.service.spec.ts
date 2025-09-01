import { TestBed } from '@angular/core/testing';
import { ResourcesApiService } from './resources.api.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('ResourcesApiService', () => {

   // eslint-disable-next-line @typescript-eslint/no-unused-vars
   let service: ResourcesApiService;
  
    beforeEach(() => {
      TestBed.configureTestingModule({
      imports: [],
      providers: [ResourcesApiService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
  });
      service = TestBed.inject(ResourcesApiService);
    });

  it('should create an instance', () => {
    expect(new ResourcesApiService()).toBeTruthy();
  });
});
