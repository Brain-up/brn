import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { DataShareService } from './data-share.service';

describe('DataShareService', () => {
  let service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DataShareService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // it('should get data', fakeAsync(() => {
  //   service.data$.subscribe(data => {
  //     tick();
  //     expect(service.data$).toBe(data)
  //   })
  // }))
});
