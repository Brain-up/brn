import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DailyTimeTableComponent } from './daily-time-table.component';
import { TranslateModule } from '@ngx-translate/core';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { AdminApiServiceFake } from '@admin/services/api/admin-api.service.fake';
import { AdminApiService } from '@admin/services/api/admin-api.service';

describe('DailyTimeTableComponent', () => {
  const responseDelayInMs = 0;
  let component: DailyTimeTableComponent;
  let fixture: ComponentFixture<DailyTimeTableComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DailyTimeTableComponent],
      imports: [TranslateModule.forRoot()],
      providers: [
        {
          provide: AdminApiService,
          useFactory: () => new AdminApiServiceFake({responseDelayInMs}),
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });
    fixture = TestBed.createComponent(DailyTimeTableComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
