import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { RouterTestingModule } from "@angular/router/testing";
import { AuthenticationApiService } from "@auth/services/api/authentication-api.service";
import { TranslateModule } from "@ngx-translate/core";
import { TokenService } from "@root/services/token.service";
import { ALocaleStorage } from "@shared/storages/local-storage";
import { environment } from "src/environments/environment";
import { AdminComponent } from "./admin.component";
import { AngularFireModule } from "@angular/fire/compat";

const userCredential = {
  user: {
    displayName: "Admin name",
  },
};

describe("AdminComponent", () => {
  let fixture: ComponentFixture<AdminComponent>;
  let component: AdminComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        AngularFireModule.initializeApp(environment.firebaseConfig),
        RouterTestingModule,
        TranslateModule.forRoot(),
        AdminComponent,
      ],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [AuthenticationApiService, TokenService],
    });

    fixture = TestBed.createComponent(AdminComponent);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should log out user on sign out", () => {
    const authService = fixture.debugElement.injector.get(
      AuthenticationApiService
    );

    const authSpy = spyOn(authService, "signOut").and.callThrough();
    const componentSpy = spyOn(component, "logout").and.callThrough();

    expect(authSpy).not.toHaveBeenCalled();
    expect(componentSpy).not.toHaveBeenCalled();

    fixture.detectChanges();

    component.logout();
    expect(authSpy).toHaveBeenCalledTimes(1);
    expect(componentSpy).toHaveBeenCalledTimes(1);
  });

  it("should get logged in user name", () => {
    const authTokenBase64 = JSON.stringify(userCredential);
    ALocaleStorage.AUTH_TOKEN.set(authTokenBase64);
    component.ngOnInit();
    expect(component.adminName.user.displayName).toBe("Admin name");
  });
});
