import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthComponent } from './auth.component';

describe('AuthComponent', () => {
  let fixture: ComponentFixture<AuthComponent>;
  let component: AuthComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AuthComponent],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(AuthComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
