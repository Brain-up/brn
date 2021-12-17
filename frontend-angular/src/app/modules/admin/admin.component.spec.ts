import { PipesModule } from './../shared/pipes/pipes.module';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { AdminComponent } from './admin.component';
import { TokenService } from '@root/services/token.service';
import { Router } from '@angular/router';
import { AUTH_PAGE_URL } from '@shared/constants/common-constants';
import { of } from 'rxjs';

describe('AdminComponent', () => {
  let fixture: ComponentFixture<AdminComponent>;
  let component: AdminComponent;
  const routerSpy = { navigate: jasmine.createSpy('navigate') };

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AdminComponent],
      imports: [PipesModule],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        {
          provide: AuthenticationApiService,
          useValue: {},
        },
        { provide: Router, useValue: routerSpy },
        { provide: TokenService, useValue: {} },
      ],
    });

    fixture = TestBed.createComponent(AdminComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should log out user on sign out', () => {
  });

  it('should get logged in user name', () => {
    // component.ngOnInit();
    // expect(component.adminName).not.toBe(undefined);
  });
});
