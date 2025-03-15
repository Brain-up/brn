import { Task } from "@admin/models/exercise";
import { AdminApiService } from "@admin/services/api/admin-api.service";
import { GroupApiService } from "@admin/services/api/group-api.service";
import { SeriesApiService } from "@admin/services/api/series-api.service";
import { SubGroupApiService } from "@admin/services/api/sub-group-api.service";
import { ChangeDetectorRef } from "@angular/core";
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from "@angular/core/testing";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { TranslateModule } from "@ngx-translate/core";
import { of, Subscription } from "rxjs";
import { ExercisesComponent } from "./exercises.component";

describe("ExercisesComponent", () => {
  let component: ExercisesComponent;
  let fixture: ComponentFixture<ExercisesComponent>;

  const adminApiServiceMock = jasmine.createSpyObj("AdminApiService", {
    getExercisesBySubGroupId: of([]),
  });
  const changeDetectorRefMock = jasmine.createSpyObj("ChangeDetectorRef", [
    "detectChanges",
  ]);
  const groupApiServiceMock = jasmine.createSpyObj("GroupApiService", [
    "getGroups",
  ]);
  const seriesApiServiceMock = jasmine.createSpyObj("SeriesApiService", [
    "getSeriesByGroupId",
  ]);
  const subGroupApiServiceMock = jasmine.createSpyObj("SubGroupApiService", [
    "getSubgroupsBySeriesId",
  ]);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        ExercisesComponent,
        TranslateModule.forRoot(),
      ],
      providers: [
        {
          provide: AdminApiService,
          useValue: adminApiServiceMock,
        },
        {
          provide: ChangeDetectorRef,
          useValue: changeDetectorRefMock,
        },
        { provide: GroupApiService, useValue: groupApiServiceMock },
        { provide: SeriesApiService, useValue: seriesApiServiceMock },
        { provide: SubGroupApiService, useValue: subGroupApiServiceMock },
        { provide: AdminApiService, useValue: adminApiServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ExercisesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  describe("should call ngoninit", () => {
    it("should set data source with displayedColumns", () => {
      const array = [
        "id",
        "seriesId",
        "name",
        "level",
        "noise",
        "noiseSound",
        "tasks",
        "available",
      ];
      component.ngOnInit();
      expect(component.displayedColumns).toEqual(array);
    });

    it("should not set initial exercises if an id is missing", fakeAsync(() => {
      component.showExercises = true;
      component.seriesName$.next("1234");
      component[`groupId$`].next("1234");
      component[`subGroupId$`].next(1234);

      component.ngOnInit();
      tick();
      expect(component.showExercises).toEqual(true);
    }));
  });

  it("should not set task matrix of no tasks", () => {
    const tasks: Task[] = [];
    expect(component.getMatrixFromTasks(tasks)).toEqual("");
  });

  it("should set task matrix", () => {
    const tasks: Task[] = [
      {
        id: 1234,
        level: 1234,
        exerciseType: "type",
        name: "name",
        serialNumber: 1234,
        answerOptions: [
          {
            id: 154,
            audioFileUrl: "/audio/ru-ru/filipp/1/2.ogg",
            word: "test1",
            wordType: "AUDIOMETRY_WORD",
            pictureFileUrl: "",
            soundsCount: 0,
          },
          {
            id: 150,
            audioFileUrl: "/audio/ru-ru/filipp/1/5.ogg",
            word: "test2",
            wordType: "AUDIOMETRY_WORD",
            pictureFileUrl: "",
            soundsCount: 0,
          },
        ],
      },
    ];
    expect(component.getMatrixFromTasks(tasks)).toEqual("test1 test2" + "\n");
  });

  it("should set the excercises hidden", () => {
    component.showExercises = true;
    component[`hideExercisesTable`]();
    expect(component.showExercises).toEqual(false);
  });

  it("should unsubscribe when destoryed", () => {
    component[`subscription`] = new Subscription();
    const spyDestroy = spyOn(Subscription.prototype, "unsubscribe");
    component.ngOnDestroy();
    expect(spyDestroy).toHaveBeenCalledTimes(1);
  });

  it("should change group", () => {
    const nextSpy = spyOn(component[`groupId$`], "next");
    component.onGroupChange("groupId");
    expect(nextSpy).toHaveBeenCalled();
  });

  it("should change series", () => {
    const nextSpy = spyOn(component[`seriesName$`], "next");
    component.onSeriesChange("seriesName");
    expect(nextSpy).toHaveBeenCalled();
  });

  it("should change subgroup", () => {
    const nextSpy = spyOn(component[`subGroupId$`], "next");
    component.onSubGroupChange(1234);
    expect(nextSpy).toHaveBeenCalled();
  });

  it("should log enable changed", () => {
    const consoleSpy = spyOn(window.console, "log");
    component.isEnableChanged("exercise", true);
    expect(consoleSpy).toHaveBeenCalled();
  });
});
