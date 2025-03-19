import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { AppComponent } from "./app.component";
import { TranslateService } from "@ngx-translate/core";
import { SvgIconsRegistrarService } from "@root/services/svg-icons-registrar.service";

describe("AppComponent", () => {
  let fixture: ComponentFixture<AppComponent>;
  let component: AppComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        {
          provide: TranslateService,
          useValue: {
            setDefaultLang: (_lang: string) => {},
            defaultLang: "en",
          },
        },
        {
          provide: SvgIconsRegistrarService,
          useValue: { registerIcons: () => {} },
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
