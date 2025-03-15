import { AdminApiService } from "@admin/services/api/admin-api.service";
import { GroupApiService } from "@admin/services/api/group-api.service";
import { SeriesApiService } from "@admin/services/api/series-api.service";
import { SubGroupApiService } from "@admin/services/api/sub-group-api.service";
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { Subject } from "rxjs";
import { SelectPanelComponent } from "./select-panel.component";
import { TranslateModule } from "@ngx-translate/core";

describe("SelectPanelComponent", () => {
  let fixture: ComponentFixture<SelectPanelComponent>;
  let component: SelectPanelComponent;

  let groupApiServiceMock;
  let seriesApiServiceMock;
  let subGroupApiServiceMock;
  let adminApiServiceMock;

  beforeEach(() => {
    groupApiServiceMock = jasmine.createSpyObj("GroupApiService", [
      "getGroups",
    ]);
    seriesApiServiceMock = jasmine.createSpyObj("SeriesApiService", [
      "getSeriesByGroupId",
    ]);
    subGroupApiServiceMock = jasmine.createSpyObj("SubGroupApiService", [
      "getSubgroupsBySeriesId",
    ]);
    adminApiServiceMock = jasmine.createSpyObj("AdminApiService", [
      "getExercisesBySubGroupId",
    ]);

    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, TranslateModule.forRoot(), SelectPanelComponent],
      providers: [
        { provide: GroupApiService, useValue: groupApiServiceMock },
        { provide: SeriesApiService, useValue: seriesApiServiceMock },
        { provide: SubGroupApiService, useValue: subGroupApiServiceMock },
        { provide: AdminApiService, useValue: adminApiServiceMock },
      ],
    });

    fixture = TestBed.createComponent(SelectPanelComponent);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("hook ngOnInit", fakeAsync(() => {
    component.groupId = "groupId";
    component.seriesId = "seriesId";
    component.subGroupId = "subGroupId";
    component.ngOnInit();
    tick();
    expect(component.groupsControl).toBeTruthy();
    expect(component.seriesControl).toBeTruthy();
    expect(component.subGroupsControl).toBeTruthy();
  }));

  it("should unsubscribe when destoryed", () => {
    component.ngUnsubscribe = new Subject();
    const spyDestroy = spyOn(Subject.prototype, "next");
    component.ngOnDestroy();
    expect(spyDestroy).toHaveBeenCalledTimes(1);
  });
});
