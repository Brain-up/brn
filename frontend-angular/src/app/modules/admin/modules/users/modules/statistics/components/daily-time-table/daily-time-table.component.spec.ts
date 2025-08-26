import { AdminApiService } from "@admin/services/api/admin-api.service";
import { AdminApiServiceFake } from "@admin/services/api/admin-api.service.fake";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { DailyTimeTableComponent } from "./daily-time-table.component";

describe("DailyTimeTableComponent", () => {
  const responseDelayInMs = 0;
  let component: DailyTimeTableComponent;
  let fixture: ComponentFixture<DailyTimeTableComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), DailyTimeTableComponent],
      providers: [
        {
          provide: AdminApiService,
          useFactory: () => new AdminApiServiceFake({ responseDelayInMs }),
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });
    fixture = TestBed.createComponent(DailyTimeTableComponent);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
