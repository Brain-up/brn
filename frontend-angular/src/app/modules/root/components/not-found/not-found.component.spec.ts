import { ComponentFixture, TestBed } from "@angular/core/testing";
import { NotFoundComponent } from "./not-found.component";
import { TranslateModule } from "@ngx-translate/core";
import { provideRouter, RouterLink } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";

describe("NotFoundComponent", () => {
  let component: NotFoundComponent;
  let fixture: ComponentFixture<NotFoundComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NotFoundComponent,
        TranslateModule.forRoot(),
      ],
      providers: [
        provideRouter([]),
        RouterLink,
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NotFoundComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
