import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { TranslateModule } from '@ngx-translate/core';
import { AuthTokenService } from '@root/services/auth-token.service';
import { SnackBarService } from '@root/services/snack-bar.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [TranslateModule.forRoot()],
      providers: [
        { provide: Router, useValue: {} },
        { provide: FormBuilder, useValue: {} },
        { provide: AuthenticationApiService, useValue: {} },
        { provide: AuthTokenService, useValue: {} },
        { provide: SnackBarService, useValue: {} },
      ],
    });

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
