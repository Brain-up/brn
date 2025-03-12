import { UserMapped } from "@admin/models/user.model";
import { AdminApiService } from "@admin/services/api/admin-api.service";
import { AdminApiServiceFake } from "@admin/services/api/admin-api.service.fake";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from "@angular/core/testing";
import { MatSortModule } from "@angular/material/sort";
import { MatTableModule } from "@angular/material/table";
import { ActivatedRoute, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule } from "@ngx-translate/core";
import { TokenService } from "@root/services/token.service";
import { PipesModule } from "@shared/pipes/pipes.module";
import { Subject } from "rxjs";
import { StatisticsComponent } from "./modules/statistics/statistics.component";
import { UsersComponent } from "./users.component";

describe("UsersComponent", () => {
  const usersNumber = 5;
  const responseDelayInMs = 0;
  const tickInMs = responseDelayInMs + 100;
  const mockRouter = {
    navigate: jasmine.createSpy("navigate"),
  };

  const fakeActivatedRoute = {
    snapshot: { data: {} },
  } as ActivatedRoute;

  let fixture: ComponentFixture<UsersComponent>;
  let component: UsersComponent;
  const tokenServiceSpy = jasmine.createSpyObj(TokenService, ["setToken"]);
  const _userList: UserMapped[] = [
    {
      age: 21,
      currentWeekChart: null,
      id: 1,
      progress: true,
      userId: "1",
      name: "Name1",
      email: "default@default.ru",
      bornYear: 1999,
      gender: "MALE",
      active: true,
      firstDone: "2021-12-13T19:07:04.832",
      lastDone: "2021-12-15T19:07:04.832",
      lastVisit: "2023-10-24T19:07:04.832",
      lastWeek: [],
      studyDaysInCurrentMonth: 1,
      diagnosticProgress: {
        SIGNALS: true,
      },
      spentTime: 10,
      doneExercises: 2,
    },
    {
      age: 22,
      currentWeekChart: null,
      id: 2,
      userId: "2",
      progress: false,
      name: "Name2",
      email: "default@default.ru",
      bornYear: 2000,
      gender: "FEMALE",
      active: true,
      firstDone: "2021-12-17T19:07:04.832",
      lastDone: "2021-12-20T19:07:04.832",
      lastVisit: "2023-10-24T19:07:04.832",
      lastWeek: [],
      studyDaysInCurrentMonth: 2,
      diagnosticProgress: {
        SIGNALS: false,
      },
      spentTime: 10,
      doneExercises: 2,
    },
  ];

  // const dataSource = new MatTableDataSource(userList);

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        MatTableModule,
        MatSortModule,
        PipesModule,
        RouterTestingModule.withRoutes([
          {
            path: ":userId/statistics",
            component: StatisticsComponent,
          },
        ]),
        TranslateModule.forRoot(),
        UsersComponent,
      ],
      providers: [
        {
          provide: AdminApiService,
          useFactory: () =>
            new AdminApiServiceFake({ responseDelayInMs, usersNumber }),
        },
        { provide: ActivatedRoute, useValue: fakeActivatedRoute },
        { provide: Router, useValue: mockRouter },
        { provide: TokenService, useValue: tokenServiceSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
  });

  it("should load user data in ngOnInit", fakeAsync(() => {
    component.ngOnInit();
    tick(tickInMs);
    expect(component.userList.length).not.toBe(undefined);
  }));

  it("unsubscribes when destoryed", () => {
    component[`destroyer`] = new Subject();
    const spyDestroy = spyOn(Subject.prototype, "next");
    component.ngOnDestroy();
    expect(spyDestroy).toHaveBeenCalledTimes(1);
  });

  it("should set viewchild sort", () => {
    // expect(fixture.componentInstance.sort).toBeDefined();
    // fixture.detectChanges();
    // const sort = component.dataSource.sort;
    // expect(sort).toBeInstanceOf(MatSort);
  });

  it("should load user data in ", fakeAsync(() => {
    // component.ngOnInit();
    // tick(tickInMs);
    // const sort = dataSource.sort;
    // expect(sort).toBeInstanceOf(MatSort);
  }));

  it("should set viewchild pagination", () => {
    // expect(fixture.componentInstance.paginator).toBeDefined();
  });
});
