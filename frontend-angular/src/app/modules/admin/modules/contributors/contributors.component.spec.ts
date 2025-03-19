import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ContributorsComponent } from "./contributors.component";
import { of } from "rxjs";
import { TranslateModule } from "@ngx-translate/core";
import { ContributorApiService } from "@admin/services/api/contributor-api.service";
import { ActivatedRoute } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { Component, NO_ERRORS_SCHEMA } from "@angular/core";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";

@Component({
  template: "",
})
class DummyComponent {}

describe("ContributorsComponent", () => {
  let component: ContributorsComponent;
  let fixture: ComponentFixture<ContributorsComponent>;
  const mockContributors = [
    {
      id: 1,
      name: "Petr",
      description: "FE",
      company: "EPAM",
      pictureUrl: "HTTP/",
      contacts: [
        {
          type: "phone",
          value: "1234567",
        },
      ],
      type: "DEVELOPER",
      contribution: 100,
      active: true,
    },
  ];
  const mockedRoutes = {
    path: "contributor",
    component: DummyComponent,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        TranslateModule.forRoot(),
        RouterTestingModule.withRoutes([mockedRoutes]),
        ContributorsComponent,
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {},
        },
        {
          provide: ContributorApiService,
          useValue: {
            getContributors: () => of(mockContributors),
          },
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContributorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should get contributors from server", () => {
    expect(component.contributorsList).toEqual(mockContributors);
  });
});
