import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { UntypedFormBuilder } from '@angular/forms';
import { LoginComponent } from './login.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { SnackBarService } from '@root/services/snack-bar.service';
import { TranslateModule } from '@ngx-translate/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
require('firebase/auth');

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  const formBuilder: UntypedFormBuilder = new UntypedFormBuilder();
  const mockSnackbar = jasmine.createSpyObj(['open']);

  const mockAuthenticationApiService: any = {
    loginWithEmail: () => {
      return { user: 'test user', idToken: 'token' };
    },
  };

  const routerStub: Router = jasmine.createSpyObj('Router', ['navigate']);
  const authStub: AuthenticationApiService = jasmine.createSpyObj(
    'RegistrationService',
    ['loginWithEmail'],
  );

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [MatSnackBarModule, TranslateModule.forRoot()],
      providers: [
        { provide: Router, useValue: {} },
        { provide: UntypedFormBuilder, useValue: formBuilder },
        {
          provide: AuthenticationApiService,
          useValue: { mockAuthenticationApiService },
        },
        { provide: SnackBarService, useValue: mockSnackbar },
      ],
    });

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;

    component.loginForm = formBuilder.group({
      grant_type: 'password',
      email: 'test@test.com',
      password: 'Test User',
    });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // it('should open error message on login', () => {
  //   const spyAuth = spyOn(mockAuthenticationApiService, 'loginWithEmail').and.returnValue(of({}));
  //   component.login();
  //   expect(mockSnackbar.open).toHaveBeenCalled();
  //   expect(spyAuth).toHaveBeenCalled();
  // });

  // it('should navigate on promise - success', fakeAsync(() => {
  //   const email = 'test@test.com';
  //   const password = 'pass';

  //   const spy = (<jasmine.Spy>routerStub.navigate).and.returnValue(Promise.resolve());
  //   (<jasmine.Spy>authStub.loginWithEmail).and.returnValue(Promise.resolve(['']));
  //   component.login();

  //   tick();
  //   expect(spy).toHaveBeenCalledWith(['/']);
  // }));
});
